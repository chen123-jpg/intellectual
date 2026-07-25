package com.intellectual.controller;

import com.intellectual.annotation.RequirePermission;
import com.intellectual.model.dto.Result;
import com.intellectual.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户列表（需 system:user:list 权限）
     */
    @RequirePermission("system:user:list")
    @GetMapping("/list")
    public Result listUsers() {
        return Result.success(userService.list());
    }
}
