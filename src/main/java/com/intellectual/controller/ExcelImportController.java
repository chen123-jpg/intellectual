package com.intellectual.controller;

import com.intellectual.annotation.RequirePermission;
import com.intellectual.model.dto.Result;
import com.intellectual.service.UserImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/excel")
public class ExcelImportController {

    @Autowired
    private UserImportService userImportService;

    @RequirePermission("system:user:import")
    @PostMapping("/import")
    public Result<String> importUsers(@RequestParam("file") MultipartFile file) {
        return Result.success(userImportService.importUsers(file));
    }
}
