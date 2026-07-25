package com.intellectual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellectual.annotation.RequirePermission;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.Patent;
import com.intellectual.service.PatentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * P表（专利信息表）控制器
 */
@RestController
@RequestMapping("/api/ptable")
public class PtableController {

    @Autowired
    private PatentService patentService;

    /** 分页列表 */
    @RequirePermission("patent:patent:list")
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String patentName,
                       @RequestParam(required = false) String patentType,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String internalNo) {
        LambdaQueryWrapper<Patent> wrapper = new LambdaQueryWrapper<Patent>()
                .like(patentName != null, Patent::getPatentName, patentName)
                .eq(patentType != null, Patent::getPatentType, patentType)
                .eq(status != null, Patent::getStatus, status)
                .eq(internalNo != null, Patent::getInternalNo, internalNo)
                .orderByDesc(Patent::getCreateTime);
        List<Patent> all = patentService.list(wrapper);
        int total = all.size();
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, total);
        List<Patent> page = from < total ? all.subList(from, to) : List.of();
        Map<String, Object> result = new HashMap<>();
        result.put("records", page);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    /** 详情 */
    @RequirePermission("patent:patent:query")
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Patent patent = patentService.getById(id);
        if (patent == null) {
            return Result.fail("专利记录不存在");
        }
        return Result.success(patent);
    }

    /** 新增 */
    @RequirePermission("patent:patent:add")
    @PostMapping
    public Result add(@RequestBody Patent patent) {
        patentService.save(patent);
        return Result.success(patent, "新增成功");
    }

    /** 修改 */
    @RequirePermission("patent:patent:edit")
    @PutMapping
    public Result update(@RequestBody Patent patent) {
        if (patent.getId() == null) {
            return Result.fail("ID不能为空");
        }
        patentService.updateById(patent);
        return Result.success(patent, "修改成功");
    }

    /** 删除 */
    @RequirePermission("patent:patent:delete")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        patentService.removeById(id);
        return Result.successMsg("删除成功");
    }

    /** 批量删除 */
    @RequirePermission("patent:patent:delete")
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        patentService.removeByIds(ids);
        return Result.successMsg("批量删除成功");
    }

    /** 全部列表（不分页） */
    @RequirePermission("patent:patent:list")
    @GetMapping("/all")
    public Result all() {
        return Result.success(patentService.list(
                new LambdaQueryWrapper<Patent>().orderByDesc(Patent::getCreateTime)));
    }
}
