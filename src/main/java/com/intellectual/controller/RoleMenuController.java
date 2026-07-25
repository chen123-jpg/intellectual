package com.intellectual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellectual.annotation.RequirePermission;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.RoleMenu;
import com.intellectual.service.RoleMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色和菜单关联表 1-N 前端控制器
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
@RestController
@RequestMapping("/sys-role-menu")
public class RoleMenuController {

    @Autowired
    private RoleMenuService roleMenuService;

    /** 分页列表 */
    @RequirePermission("system:roleMenu:list")
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) Long roleId,
                       @RequestParam(required = false) Long menuId) {
        LambdaQueryWrapper<RoleMenu> wrapper = new LambdaQueryWrapper<RoleMenu>()
                .eq(roleId != null, RoleMenu::getRoleId, roleId)
                .eq(menuId != null, RoleMenu::getMenuId, menuId);
        List<RoleMenu> all = roleMenuService.list(wrapper);
        int total = all.size();
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, total);
        List<RoleMenu> page = from < total ? all.subList(from, to) : List.of();
        Map<String, Object> result = new HashMap<>();
        result.put("records", page);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    /** 全部列表（不分页） */
    @RequirePermission("system:roleMenu:list")
    @GetMapping("/all")
    public Result all() {
        return Result.success(roleMenuService.list());
    }

    /** 新增 */
    @RequirePermission("system:roleMenu:add")
    @PostMapping
    public Result add(@RequestBody RoleMenu roleMenu) {
        roleMenuService.save(roleMenu);
        return Result.success(roleMenu, "新增成功");
    }

    /** 删除（按菜单ID） */
    @RequirePermission("system:roleMenu:delete")
    @DeleteMapping("/{menuId}")
    public Result delete(@PathVariable Long menuId) {
        roleMenuService.removeById(menuId);
        return Result.successMsg("删除成功");
    }

    /** 批量删除 */
    @RequirePermission("system:roleMenu:delete")
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        roleMenuService.removeByIds(ids);
        return Result.successMsg("批量删除成功");
    }
}
