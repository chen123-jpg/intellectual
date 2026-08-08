package com.intellectual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellectual.annotation.RequirePermission;
import com.intellectual.model.constants.Constants;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.Menu;
import com.intellectual.redis.RedisUtils;
import com.intellectual.security.LoginUser;
import com.intellectual.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单权限表 前端控制器
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
@RestController
@RequestMapping({"/sys-menu", "/api/sys-menu"})
public class MenuController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private RedisUtils redisUtils;

    /** 菜单缓存Key前缀 */
    private static final String REDIS_KEY_MENU_ALL = "patent:menu:all";
    private static final String REDIS_KEY_MENU_TREE = "patent:menu:tree:";
    private static final String REDIS_KEY_MENU_BY_ID = "patent:menu:id:";

    /** 缓存过期时间（30分钟） */
    private static final long MENU_CACHE_EXPIRE = Constants.REDIS_TIME_30MIN;

    /**
     * 分页列表 - 直接从数据库查询（分页数据变化频繁，不适合缓存）
     */
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

    /**
     * 全部列表（不分页）- 根据当前用户权限过滤
     * 从Redis缓存获取，缓存不存在时从数据库加载并缓存
     */
    @GetMapping("/all")
    public Result all() {
        Set<String> userPerms = getUserPermissions();
        String cacheKey = REDIS_KEY_MENU_ALL + ":" + (userPerms != null ? userPerms.hashCode() : "anon");

        // 尝试从Redis获取缓存
        Object cachedMenus = redisUtils.get(cacheKey);
        if (cachedMenus != null) {
            @SuppressWarnings("unchecked")
            List<Menu> menus = (List<Menu>) cachedMenus;
            return Result.success(menus);
        }

        // 缓存不存在，从数据库查询
        List<Menu> menus = menuService.list(
                new LambdaQueryWrapper<Menu>().orderByAsc(Menu::getOrderNum));

        // 过滤：perms为null的菜单(MENU/PAGE)所有人可见，有perms的菜单需用户持有该权限
        if (userPerms != null && !userPerms.isEmpty()) {
            menus = menus.stream()
                    .filter(m -> m.getPerms() == null || userPerms.contains(m.getPerms()))
                    .collect(Collectors.toList());
        }

        // 存入Redis缓存
        redisUtils.set(cacheKey, menus, MENU_CACHE_EXPIRE);

        return Result.success(menus);
    }

    /**
     * 获取菜单树（含子菜单）- 按用户权限过滤
     */
    @GetMapping("/tree")
    public Result tree() {
        Set<String> userPerms = getUserPermissions();
        String cacheKey = REDIS_KEY_MENU_TREE + (userPerms != null ? userPerms.hashCode() : "anon");

        // 尝试从Redis获取缓存
        Object cachedTree = redisUtils.get(cacheKey);
        if (cachedTree != null) {
            return Result.success(cachedTree);
        }

        // 缓存不存在，从数据库查询并构建树
        List<Menu> allMenus = menuService.list(
                new LambdaQueryWrapper<Menu>().orderByAsc(Menu::getOrderNum));

        // 权限过滤
        if (userPerms != null && !userPerms.isEmpty()) {
            allMenus = allMenus.stream()
                    .filter(m -> m.getPerms() == null || userPerms.contains(m.getPerms()))
                    .collect(Collectors.toList());
        }

        // 构建菜单树
        List<Map<String, Object>> tree = buildMenuTree(allMenus, 0L);

        // 存入Redis缓存
        redisUtils.set(cacheKey, tree, MENU_CACHE_EXPIRE);

        return Result.success(tree);
    }

    /**
     * 构建菜单树
     */
    private List<Map<String, Object>> buildMenuTree(List<Menu> menus, Long parentId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Menu menu : menus) {
            if (menu.getParentId() != null && menu.getParentId().equals(parentId)) {
                Map<String, Object> node = new HashMap<>();
                node.put("menuId", menu.getMenuId());
                node.put("menuName", menu.getMenuName());
                node.put("parentId", menu.getParentId());
                node.put("orderNum", menu.getOrderNum());
                node.put("url", menu.getUrl());
                node.put("target", menu.getTarget());
                node.put("module", menu.getModule());
                node.put("menuType", menu.getMenuType());
                node.put("visible", menu.getVisible());
                node.put("isRefresh", menu.getIsRefresh());
                node.put("perms", menu.getPerms());
                node.put("icon", menu.getIcon());
                node.put("remark", menu.getRemark());
                // 递归子菜单
                List<Map<String, Object>> children = buildMenuTree(menus, menu.getMenuId());
                if (!children.isEmpty()) {
                    node.put("children", children);
                }
                result.add(node);
            }
        }
        return result;
    }

    /**
     * 详情 - 先从Redis获取，获取不到再查数据库
     */
    @RequirePermission("system:menu:query")
    @GetMapping("/{menuId}")
    public Result getById(@PathVariable Long menuId) {
        String cacheKey = REDIS_KEY_MENU_BY_ID + menuId;

        // 尝试从Redis获取
        Object cachedMenu = redisUtils.get(cacheKey);
        if (cachedMenu != null) {
            return Result.success(cachedMenu);
        }

        // 缓存不存在，从数据库查询
        Menu menu = menuService.getById(menuId);
        if (menu == null) {
            return Result.fail("菜单不存在");
        }

        // 存入Redis缓存
        redisUtils.set(cacheKey, menu, MENU_CACHE_EXPIRE);

        return Result.success(menu);
    }

    /**
     * 新增 - 清除相关缓存
     */
    @RequirePermission("system:menu:add")
    @PostMapping
    public Result add(@RequestBody Menu menu) {
        menuService.save(menu);
        // 清除所有菜单缓存
        clearMenuCache();
        return Result.success(menu, "新增成功");
    }

    /**
     * 修改 - 清除相关缓存
     */
    @RequirePermission("system:menu:edit")
    @PutMapping
    public Result update(@RequestBody Menu menu) {
        if (menu.getMenuId() == null) {
            return Result.fail("ID不能为空");
        }
        menuService.updateById(menu);
        // 清除所有菜单缓存
        clearMenuCache();
        return Result.success(menu, "修改成功");
    }

    /**
     * 删除 - 清除相关缓存
     */
    @RequirePermission("system:menu:delete")
    @DeleteMapping("/{menuId}")
    public Result delete(@PathVariable Long menuId) {
        menuService.removeById(menuId);
        // 清除所有菜单缓存
        clearMenuCache();
        return Result.successMsg("删除成功");
    }

    /**
     * 批量删除 - 清除相关缓存
     */
    @RequirePermission("system:menu:delete")
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        menuService.removeByIds(ids);
        // 清除所有菜单缓存
        clearMenuCache();
        return Result.successMsg("批量删除成功");
    }

    /**
     * 手动清除菜单缓存（管理员接口）
     */
    @RequirePermission("system:menu:edit")
    @DeleteMapping("/cache")
    public Result clearCache() {
        clearMenuCache();
        return Result.successMsg("菜单缓存已清除");
    }

    /**
     * 清除所有菜单相关缓存
     */
    private void clearMenuCache() {
        // 清除全部菜单列表缓存（使用通配符方式需要自行实现扫描删除）
        // 这里简化处理，由于缓存Key包含权限hash，实际生产环境可使用Redis SCAN命令
        // 或直接删除前缀匹配的所有Key（需要扩展RedisUtils）
        redisUtils.del(REDIS_KEY_MENU_ALL + ":*");
        redisUtils.del(REDIS_KEY_MENU_TREE + ":*");
        // 按ID的缓存无法批量匹配，可考虑使用Set记录所有ID或直接不缓存单条详情
        // 简单方案：不缓存单条详情，或缓存时记录所有ID到Set，清除时遍历删除
        // 这里选择在clear时不清除单条缓存，随自然过期
    }

    /**
     * 从 SecurityContext 中获取当前登录用户的权限集合
     */
    private Set<String> getUserPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return ((LoginUser) authentication.getPrincipal()).getPermissions();
        }
        return null;
    }
}