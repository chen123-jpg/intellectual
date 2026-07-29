package com.intellectual.service;

import org.springframework.web.multipart.MultipartFile;

public interface UserImportService {

    /**
     * 从Excel文件批量导入用户
     * @param file Excel文件
     * @return 导入结果摘要
     */
    String importUsers(MultipartFile file);
}
