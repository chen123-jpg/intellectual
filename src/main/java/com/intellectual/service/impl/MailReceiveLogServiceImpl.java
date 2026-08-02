package com.intellectual.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.MailReceiveLog;
import com.intellectual.service.MailReceiveLogService;
import com.intellectual.mapper.MailReceiveLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邮件接收记录表 服务实现类
 *
 * @author 陈创
 * @since 2026-08-02
 */
@Service
public class MailReceiveLogServiceImpl extends ServiceImpl<MailReceiveLogMapper, MailReceiveLog> implements MailReceiveLogService {

    private final MailReceiveLogMapper mailReceiveLogMapper;

    public MailReceiveLogServiceImpl(MailReceiveLogMapper mailReceiveLogMapper) {
        this.mailReceiveLogMapper = mailReceiveLogMapper;
    }

    @Override
    public Result getPage(Long userId, Integer current, Integer size) {
        var query = Wrappers.lambdaQuery(MailReceiveLog.class)
                .eq(userId != null, MailReceiveLog::getSenderUserId, userId)
                .orderByDesc(MailReceiveLog::getCreateTime);
        Page<MailReceiveLog> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        Page<MailReceiveLog> result = mailReceiveLogMapper.selectPage(page, query);
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        return Result.success(data);
    }

    @Override
    public Result getById(Long id) {
        MailReceiveLog log = mailReceiveLogMapper.selectById(id);
        if (log == null) {
            return Result.fail("记录不存在");
        }
        return Result.success(log);
    }

    @Override
    public Result deleteById(Long id) {
        MailReceiveLog log = mailReceiveLogMapper.selectById(id);
        if (log == null) {
            return Result.fail("记录不存在");
        }
        mailReceiveLogMapper.deleteById(id);
        return Result.successMsg("删除成功");
    }

    @Override
    public Result batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail("请选择要删除的记录");
        }
        mailReceiveLogMapper.deleteByIds(ids);
        return Result.successMsg("批量删除成功");
    }
}
