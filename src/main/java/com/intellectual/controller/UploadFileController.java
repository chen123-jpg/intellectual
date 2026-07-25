package com.intellectual.controller;

import com.intellectual.model.dto.Result;
import com.intellectual.service.impl.UploadFileServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
public class UploadFileController {

    @Autowired
    private UploadFileServiceImpl uploadFileService;


    /**
     * 上传文件 - 返回带原始文件名的 URL（?name=原始文件名）
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        return uploadFileService.upload(file);
    }

    /**
     * 查看/下载文件 - 从 URL 参数中获取原始文件名，并设置响应头
     */
    @GetMapping("/files/{fileId}")   // fileId = uuid.ext
    public ResponseEntity<Resource> getFile(
            @PathVariable String fileId,
            @RequestParam(value = "name", required = false) String originalName) {
        return uploadFileService.getFile(fileId,originalName);
    }
}