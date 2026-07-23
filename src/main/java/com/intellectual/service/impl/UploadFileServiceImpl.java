package com.intellectual.service.impl;

import com.intellectual.model.dto.Result;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class UploadFileServiceImpl {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
            log.info("上传目录已创建: {}", uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录: " + uploadPath, e);
        }
    }
    public Result upload(MultipartFile file){
        if (file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // 磁盘存储名（UUID）
            String newFilename = UUID.randomUUID().toString() + extension;
            Path targetPath = uploadPath.resolve(newFilename);
            file.transferTo(targetPath.toFile());

            // ★★★ 将原始文件名编码后作为查询参数 ★★★
            String encodedName = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8);
            String fileUrl = "/files/" + newFilename + "?name=" + encodedName;

            log.info("文件上传成功: {} -> {}", originalFilename, targetPath);
            return Result.success(fileUrl);
        } catch (
                IOException e) {
            log.error("文件上传失败", e);
            return Result.fail("文件上传失败: " + e.getMessage());
        }
    }

    public ResponseEntity<Resource> getFile(String fileId, String originalName) {
        try {
            Path filePath = uploadPath.resolve(fileId).normalize();
            if (!filePath.startsWith(uploadPath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // ★★★ 使用原始文件名（若有），否则回退到磁盘文件名 ★★★
            String dispositionFilename = (originalName != null && !originalName.isEmpty())
                    ? originalName
                    : resource.getFilename();
            // 支持中文等特殊字符（RFC 5987）
            String encodedDisposition = URLEncoder.encode(dispositionFilename, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + dispositionFilename + "\"; filename*=UTF-8''" + encodedDisposition)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
