package com.intellectual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellectual.annotation.RequirePermission;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.Agent;
import com.intellectual.model.entity.Applicant;
import com.intellectual.service.AgentService;
import com.intellectual.service.ApplicantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 外部人员
 * 代理人/申请人控制器
 */
@RestController
@RequestMapping("/api")
public class ExternalPersonnelController {

    @Autowired
    private AgentService agentService;

    @Autowired
    private ApplicantService applicantService;

    /** 分页列表 */
    @RequirePermission("patent:agent:list")
    @GetMapping("/agent/list")
    public Result list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String name) {
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<Agent>()
                .like(name != null, Agent::getName, name)
                .orderByAsc(Agent::getName);
        List<Agent> all = agentService.list(wrapper);
        int total = all.size();
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, total);
        List<Agent> page = from < total ? all.subList(from, to) : List.of();
        Map<String, Object> result = new HashMap<>();
        result.put("records", page);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    /** 全部列表（不分页） */
    @RequirePermission("patent:agent:list")
    @GetMapping("/agent/all")
    public Result all() {
        return Result.success(agentService.list(
                new LambdaQueryWrapper<Agent>().orderByAsc(Agent::getName)));
    }

    /** 详情 */
    @RequirePermission("patent:agent:query")
    @GetMapping("/agent/{id}")
    public Result getById(@PathVariable Long id) {
        Agent agent = agentService.getById(id);
        return agent != null ? Result.success(agent) : Result.fail("代理人不存在");
    }

    /** 新增 */
    @RequirePermission("patent:agent:add")
    @PostMapping("/agent")
    public Result add(@RequestBody Agent agent) {
        agentService.save(agent);
        return Result.success(agent, "新增成功");
    }

    /** 修改 */
    @RequirePermission("patent:agent:edit")
    @PutMapping("/agent")
    public Result update(@RequestBody Agent agent) {
        if (agent.getId() == null) {
            return Result.fail("ID不能为空");
        }
        agentService.updateById(agent);
        return Result.success(agent, "修改成功");
    }

    /** 删除 */
    @RequirePermission("patent:agent:delete")
    @DeleteMapping("/agent/{id}")
    public Result delete(@PathVariable Long id) {
        agentService.removeById(id);
        return Result.successMsg("删除成功");
    }

    /** 批量删除 */
    @RequirePermission("patent:agent:delete")
    @DeleteMapping("/agent/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        agentService.removeByIds(ids);
        return Result.successMsg("批量删除成功");
    }

    // ==================== 申请人 ====================

    /** 申请人分页列表 */
    @RequirePermission("patent:applicant:list")
    @GetMapping("/applicant/list")
    public Result applicantList(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                @RequestParam(required = false) String name) {
        LambdaQueryWrapper<Applicant> wrapper = new LambdaQueryWrapper<Applicant>()
                .like(name != null, Applicant::getName, name)
                .orderByAsc(Applicant::getName);
        List<Applicant> all = applicantService.list(wrapper);
        int total = all.size();
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, total);
        List<Applicant> page = from < total ? all.subList(from, to) : List.of();
        Map<String, Object> result = new HashMap<>();
        result.put("records", page);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    /** 申请人全部列表（不分页） */
    @RequirePermission("patent:applicant:list")
    @GetMapping("/applicant/all")
    public Result applicantAll() {
        return Result.success(applicantService.list(
                new LambdaQueryWrapper<Applicant>().orderByAsc(Applicant::getName)));
    }

    /** 申请人详情 */
    @RequirePermission("patent:applicant:query")
    @GetMapping("/applicant/{id}")
    public Result applicantGetById(@PathVariable Long id) {
        Applicant applicant = applicantService.getById(id);
        return applicant != null ? Result.success(applicant) : Result.fail("申请人不存在");
    }

    /** 申请人新增 */
    @RequirePermission("patent:applicant:add")
    @PostMapping("/applicant")
    public Result applicantAdd(@RequestBody Applicant applicant) {
        applicantService.save(applicant);
        return Result.success(applicant, "新增成功");
    }

    /** 申请人修改 */
    @RequirePermission("patent:applicant:edit")
    @PutMapping("/applicant")
    public Result applicantUpdate(@RequestBody Applicant applicant) {
        if (applicant.getId() == null) {
            return Result.fail("ID不能为空");
        }
        applicantService.updateById(applicant);
        return Result.success(applicant, "修改成功");
    }

    /** 申请人删除 */
    @RequirePermission("patent:applicant:delete")
    @DeleteMapping("/applicant/{id}")
    public Result applicantDelete(@PathVariable Long id) {
        applicantService.removeById(id);
        return Result.successMsg("删除成功");
    }

    /** 申请人批量删除 */
    @RequirePermission("patent:applicant:delete")
    @DeleteMapping("/applicant/batch")
    public Result applicantDeleteBatch(@RequestBody List<Long> ids) {
        applicantService.removeByIds(ids);
        return Result.successMsg("批量删除成功");
    }
}
