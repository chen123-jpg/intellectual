package com.intellectual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellectual.annotation.RequirePermission;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.Menu;
import com.intellectual.model.entity.Role;
import com.intellectual.model.entity.RoleMenu;
import com.intellectual.service.MenuService;
import com.intellectual.service.RoleMenuService;
import com.intellectual.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private RoleService roleService;

    @Autowired
    private MenuService menuService;

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
        result.put("records", enrichRoleMenus(page));
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    /** 全部列表（不分页） */
    @RequirePermission("system:roleMenu:list")
    @GetMapping("/all")
    public Result all() {
        return Result.success(enrichRoleMenus(roleMenuService.list()));
    }

    private List<Map<String, Object>> enrichRoleMenus(List<RoleMenu> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        Set<Long> roleIds = list.stream().map(RoleMenu::getRoleId).collect(Collectors.toSet());
        Set<Long> menuIds = list.stream().map(RoleMenu::getMenuId).collect(Collectors.toSet());
        Map<Long, String> roleMap = roleService.listByIds(roleIds).stream()
                .collect(Collectors.toMap(Role::getRoleId, Role::getRoleName, (a, b) -> a));
        Map<Long, String> menuMap = menuService.listByIds(menuIds).stream()
                .collect(Collectors.toMap(Menu::getMenuId, Menu::getMenuName, (a, b) -> a));
        List<Map<String, Object>> result = new ArrayList<>();
        for (RoleMenu rm : list) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("roleId", rm.getRoleId());
            map.put("roleName", roleMap.getOrDefault(rm.getRoleId(), null));
            map.put("menuId", rm.getMenuId());
            map.put("menuName", menuMap.getOrDefault(rm.getMenuId(), null));
            result.add(map);
        }
        return result;
    }

    /** 新增 */
    @RequirePermission("system:roleMenu:add")
    @PostMapping
    public Result add(@RequestBody RoleMenu roleMenu) {
        roleMenuService.save(roleMenu);
        return Result.success(roleMenu, "新增成功");
    }

    /** 编辑 */
    @RequirePermission("system:roleMenu:edit")
    @PutMapping
    public Result edit(@RequestBody RoleMenu roleMenu) {
        roleMenuService.updateById(roleMenu);
        return Result.success(roleMenu, "修改成功");
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
