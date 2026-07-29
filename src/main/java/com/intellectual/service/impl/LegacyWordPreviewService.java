package com.intellectual.service.impl;

import com.intellectual.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/** 将旧版 Word .doc 安全提交到内网 Gotenberg，并返回临时 PDF。 */
@Slf4j
@Service
public class LegacyWordPreviewService {
    private static final long MAX_INPUT_SIZE = 10L * 1024 * 1024;
    private static final long MAX_OUTPUT_SIZE = 50L * 1024 * 1024;
    private static final byte[] OLE_HEADER = {
            (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0,
            (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1
    };

    private final RestClient gotenbergClient;
    private final boolean previewEnabled;
    private final long acquireTimeoutSeconds;
    private final Semaphore conversionPermits;

    @Autowired
    public LegacyWordPreviewService(
            RestClient.Builder restClientBuilder,
            @Value("${app.preview.gotenberg.enabled:false}") boolean previewEnabled,
            @Value("${app.preview.gotenberg.base-url:http://127.0.0.1:3000}") String baseUrl,
            @Value("${app.preview.gotenberg.connect-timeout-seconds:3}") long connectTimeoutSeconds,
            @Value("${app.preview.gotenberg.read-timeout-seconds:70}") long readTimeoutSeconds,
            @Value("${app.preview.gotenberg.acquire-timeout-seconds:5}") long acquireTimeoutSeconds,
            @Value("${app.preview.gotenberg.max-concurrent:2}") int maxConcurrent) {
        this(createClient(restClientBuilder, baseUrl, connectTimeoutSeconds, readTimeoutSeconds), previewEnabled,
                acquireTimeoutSeconds, maxConcurrent);
    }

    LegacyWordPreviewService(RestClient gotenbergClient, long acquireTimeoutSeconds, int maxConcurrent) {
        this(gotenbergClient, true, acquireTimeoutSeconds, maxConcurrent);
    }

    LegacyWordPreviewService(RestClient gotenbergClient, boolean previewEnabled,
                             long acquireTimeoutSeconds, int maxConcurrent) {
        this.gotenbergClient = gotenbergClient;
        this.previewEnabled = previewEnabled;
        this.acquireTimeoutSeconds = Math.max(1, acquireTimeoutSeconds);
        this.conversionPermits = new Semaphore(Math.max(1, maxConcurrent), true);
    }

    public byte[] convertDocToPdf(MultipartFile file) {
        if (!previewEnabled) {
            throw new BusinessException("DOC 在线预览暂未启用，请下载原文件查看");
        }
        byte[] source = validate(file);
        boolean acquired = false;
        try {
            acquired = conversionPermits.tryAcquire(acquireTimeoutSeconds, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BusinessException("DOC 预览任务较多，请稍后重试");
            }
            return requestConversion(source);
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("DOC 转换已中断，请重试");
        } finally {
            if (acquired) {
                conversionPermits.release();
            }
        }
    }

    private byte[] requestConversion(byte[] source) {
        ByteArrayResource resource = new ByteArrayResource(source) {
            @Override
            public String getFilename() {
                return "preview.doc";
            }
        };
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("files", resource);
        form.add("exportFormFields", "false");

        try {
            ResponseEntity<byte[]> response = gotenbergClient.post()
                    .uri("/forms/libreoffice/convert")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .accept(MediaType.APPLICATION_PDF)
                    .header("Gotenberg-Output-Filename", "preview")
                    .body(form)
                    .exchange((request, result) -> ResponseEntity
                            .status(result.getStatusCode())
                            .headers(result.getHeaders())
                            .body(result.getBody().readAllBytes()));

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Gotenberg DOC 转换失败，HTTP 状态: {}", response.getStatusCode().value());
                if (response.getStatusCode().value() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
                    throw new BusinessException("DOC 预览服务繁忙或暂不可用，请稍后重试");
                }
                if (response.getStatusCode().is4xxClientError()) {
                    throw new BusinessException("DOC 转换失败，文件可能已损坏或包含不受支持的内容");
                }
                throw new BusinessException("DOC 在线预览服务返回异常，请稍后重试");
            }
            return validatePdf(response.getBody());
        } catch (BusinessException e) {
            throw e;
        } catch (ResourceAccessException e) {
            log.warn("无法连接 Gotenberg DOC 预览服务: {}", e.getMessage());
            throw new BusinessException("DOC 在线预览服务不可用，请确认 Gotenberg 已启动");
        } catch (RestClientException e) {
            log.error("调用 Gotenberg DOC 预览服务失败", e);
            throw new BusinessException("DOC 在线预览服务调用失败，请稍后重试");
        }
    }

    private byte[] validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("预览文件不能为空");
        }
        if (file.getSize() > MAX_INPUT_SIZE) {
            throw new BusinessException("DOC 文件不能超过 10 MB");
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase(Locale.ROOT).endsWith(".doc")) {
            throw new BusinessException("该转换接口只支持旧版 .doc 文件");
        }
        try {
            byte[] bytes = file.getBytes();
            if (bytes.length < OLE_HEADER.length) {
                throw new BusinessException("文件不是有效的 .doc 文档");
            }
            for (int i = 0; i < OLE_HEADER.length; i++) {
                if (bytes[i] != OLE_HEADER[i]) {
                    throw new BusinessException("文件不是有效的 .doc 文档");
                }
            }
            return bytes;
        } catch (IOException e) {
            throw new BusinessException("DOC 文件读取失败");
        }
    }

    private byte[] validatePdf(byte[] pdf) {
        if (pdf == null || pdf.length < 5 || pdf.length > MAX_OUTPUT_SIZE) {
            throw new BusinessException("DOC 转换结果异常，请下载原文件查看");
        }
        if (pdf[0] != '%' || pdf[1] != 'P' || pdf[2] != 'D' || pdf[3] != 'F' || pdf[4] != '-') {
            throw new BusinessException("DOC 转换结果不是有效的 PDF 文件");
        }
        return pdf;
    }

    private static RestClient createClient(RestClient.Builder builder, String baseUrl,
                                           long connectTimeoutSeconds, long readTimeoutSeconds) {
        String normalizedBaseUrl = baseUrl == null || baseUrl.isBlank()
                ? "http://127.0.0.1:3000"
                : baseUrl.trim().replaceAll("/+$", "");
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(Math.max(1, connectTimeoutSeconds)));
        requestFactory.setReadTimeout(Duration.ofSeconds(Math.max(1, readTimeoutSeconds)));
        return builder.baseUrl(normalizedBaseUrl).requestFactory(requestFactory).build();
    }
}
