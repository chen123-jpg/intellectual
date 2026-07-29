package com.intellectual.controller;

import com.intellectual.exception.BusinessException;
import com.intellectual.service.impl.LegacyWordPreviewService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/** 登录用户使用的临时文件预览转换接口。 */
@RestController
@RequestMapping("/api/file-preview")
public class FilePreviewController {
    private final LegacyWordPreviewService legacyWordPreviewService;

    public FilePreviewController(LegacyWordPreviewService legacyWordPreviewService) {
        this.legacyWordPreviewService = legacyWordPreviewService;
    }

    @PostMapping(value = "/legacy-word", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ByteArrayResource> previewLegacyWord(@RequestPart("file") MultipartFile file) {
        try {
            byte[] pdf = legacyWordPreviewService.convertDocToPdf(file);
            String original = file.getOriginalFilename() == null ? "preview.doc" : file.getOriginalFilename();
            String previewName = original.replaceFirst("(?i)\\.doc$", "") + ".pdf";
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdf.length)
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                            .filename(previewName, StandardCharsets.UTF_8)
                            .build().toString())
                    .header(HttpHeaders.CACHE_CONTROL, "no-store")
                    .body(new ByteArrayResource(pdf));
        } catch (BusinessException e) {
            byte[] message = e.getMessage().getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.unprocessableEntity()
                    .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                    .contentLength(message.length)
                    .header(HttpHeaders.CACHE_CONTROL, "no-store")
                    .body(new ByteArrayResource(message));
        }
    }
}
