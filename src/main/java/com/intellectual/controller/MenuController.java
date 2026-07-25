package com.intellectual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellectual.annotation.RequirePermission;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.Menu;
import com.intellectual.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单权限表 前端控制器
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
@RestController
@RequestMapping("/sys-menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    /** 分页列表 */
    @RequirePermission("system:menu:list")
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String menuName,
                       @RequestParam(required = false) String menuType,
                       @RequestParam(required = false) String visible,
                       @RequestParam(required = false) Long parentId) {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<Menu>()
                .like(menuName != null, Menu::getMenuName, menuName)
                .eq(menuType != null, Menu::getMenuType, menuType)
                .eq(visible != null, Menu::getVisible, visible)
                .eq(parentId != null, Menu::getParentId, parentId)
                .orderByAsc(Menu::getOrderNum);
        List<Menu> all = menuService.list(wrapper);
        int total = all.size();
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, total);
        List<Menu> page = from < total ? all.subList(from, to) : List.of();
        Map<String, Object> result = new HashMap<>();
        result.put("records", page);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    /** 全部列表（不分页） */
    @RequirePermission("system:menu:list")
    @GetMapping("/all")
    public Result all() {
        return Result.success(menuService.list(
                new LambdaQueryWrapper<Menu>().orderByAsc(Menu::getOrderNum)));
    }

    /** 详情 */
    @RequirePermission("system:menu:query")
    @GetMapping("/{menuId}")
    public Result getById(@PathVariable Long menuId) {
        Menu menu = menuService.getById(menuId);
        if (menu == null) {
            return Result.fail("菜单不存在");
        }
        return Result.success(menu);
    }

    /** 新增 */
    @RequirePermission("system:menu:add")
    @PostMapping
    public Result add(@RequestBody Menu menu) {
        menuService.save(menu);
        return Result.success(menu, "新增成功");
    }

    /** 修改 */
    @RequirePermission("system:menu:edit")
    @PutMapping
    public Result update(@RequestBody Menu menu) {
        if (menu.getMenuId() == null) {
            return Result.fail("ID不能为空");
        }
        menuService.updateById(menu);
        return Result.success(menu, "修改成功");
    }

    /** 删除 */
    @RequirePermission("system:menu:delete")
    @DeleteMapping("/{menuId}")
    public Result delete(@PathVariable Long menuId) {
        menuService.removeById(menuId);
        return Result.successMsg("删除成功");
    }

    /** 批量删除 */
    @RequirePermission("system:menu:delete")
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        menuService.removeByIds(ids);
        return Result.successMsg("批量删除成功");
    }
}
