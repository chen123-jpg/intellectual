package com.intellectual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellectual.annotation.RequirePermission;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.Role;
import com.intellectual.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色信息表 前端控制器
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
@RestController
@RequestMapping("/sys-role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /** 分页列表 */
    @RequirePermission("system:role:list")
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String roleName,
                       @RequestParam(required = false) String roleKey,
                       @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<Role>()
                .like(roleName != null, Role::getRoleName, roleName)
                .like(roleKey != null, Role::getRoleKey, roleKey)
                .eq(status != null, Role::getStatus, status)
                .orderByAsc(Role::getRoleSort);
        List<Role> all = roleService.list(wrapper);
        int total = all.size();
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, total);
        List<Role> page = from < total ? all.subList(from, to) : List.of();
        Map<String, Object> result = new HashMap<>();
        result.put("records", page);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    /** 全部列表（不分页） */
    @RequirePermission("system:role:list")
    @GetMapping("/all")
    public Result all() {
        return Result.success(roleService.list(
                new LambdaQueryWrapper<Role>().orderByAsc(Role::getRoleSort)));
    }

    /** 详情 */
    @RequirePermission("system:role:query")
    @GetMapping("/{roleId}")
    public Result getById(@PathVariable Long roleId) {
        Role role = roleService.getById(roleId);
        if (role == null) {
            return Result.fail("角色不存在");
        }
        return Result.success(role);
    }

    /** 新增 */
    @RequirePermission("system:role:add")
    @PostMapping
    public Result add(@RequestBody Role role) {
        roleService.save(role);
        return Result.success(role, "新增成功");
    }

    /** 修改 */
    @RequirePermission("system:role:edit")
    @PutMapping
    public Result update(@RequestBody Role role) {
        if (role.getRoleId() == null) {
            return Result.fail("ID不能为空");
        }
        roleService.updateById(role);
        return Result.success(role, "修改成功");
    }

    /** 删除 */
    @RequirePermission("system:role:delete")
    @DeleteMapping("/{roleId}")
    public Result delete(@PathVariable Long roleId) {
        roleService.removeById(roleId);
        return Result.successMsg("删除成功");
    }

    /** 批量删除 */
    @RequirePermission("system:role:delete")
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        roleService.removeByIds(ids);
        return Result.successMsg("批量删除成功");
    }
}
