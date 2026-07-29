package com.intellectual.controller;

import com.intellectual.annotation.RequirePermission;
import com.intellectual.model.dto.Result;
import com.intellectual.model.dto.UserSaveDto;
import com.intellectual.model.vo.UserVo;
import com.intellectual.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * 用户详情（需 system:user:query 权限）
     */
    @RequirePermission("system:user:query")
    @GetMapping("/{id}")
    public Result<UserVo> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    /**
     * 新增用户（需 system:user:add 权限）
     */
    @RequirePermission("system:user:add")
    @PostMapping
    public Result addUser(@RequestBody UserSaveDto dto) {
        userService.createUser(dto);
        return Result.successMsg("新增成功");
    }

    /**
     * 修改用户（需 system:user:edit 权限）
     */
    @RequirePermission("system:user:edit")
    @PutMapping
    public Result updateUser(@RequestBody UserSaveDto dto) {
        userService.updateUser(dto);
        return Result.successMsg("修改成功");
    }

    /**
     * 删除用户（需 system:user:remove 权限）
     */
    @RequirePermission("system:user:remove")
    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.successMsg("删除成功");
    }
}
