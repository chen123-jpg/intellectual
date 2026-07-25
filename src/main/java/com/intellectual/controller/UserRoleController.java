package com.intellectual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellectual.annotation.RequirePermission;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.UserRole;
import com.intellectual.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户和角色关联表 N-1 前端控制器
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
@RestController
@RequestMapping("/sys-user-role")
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    /** 分页列表 */
    @RequirePermission("system:userRole:list")
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) Long userId,
                       @RequestParam(required = false) Long roleId) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<UserRole>()
                .eq(userId != null, UserRole::getUserId, userId)
                .eq(roleId != null, UserRole::getRoleId, roleId);
        List<UserRole> all = userRoleService.list(wrapper);
        int total = all.size();
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, total);
        List<UserRole> page = from < total ? all.subList(from, to) : List.of();
        Map<String, Object> result = new HashMap<>();
        result.put("records", page);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    /** 全部列表（不分页） */
    @RequirePermission("system:userRole:list")
    @GetMapping("/all")
    public Result all() {
        return Result.success(userRoleService.list());
    }

    /** 新增 */
    @RequirePermission("system:userRole:add")
    @PostMapping
    public Result add(@RequestBody UserRole userRole) {
        userRoleService.save(userRole);
        return Result.success(userRole, "新增成功");
    }

    /** 删除（按角色ID） */
    @RequirePermission("system:userRole:delete")
    @DeleteMapping("/{roleId}")
    public Result delete(@PathVariable Long roleId) {
        userRoleService.removeById(roleId);
        return Result.successMsg("删除成功");
    }

    /** 批量删除 */
    @RequirePermission("system:userRole:delete")
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        userRoleService.removeByIds(ids);
        return Result.successMsg("批量删除成功");
    }
}
