package com.intellectual.service;

import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.MailReceiveLog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 邮件接收记录表 服务类接口
 *
 * @author 陈创
 * @since 2026-08-02
 */
public interface MailReceiveLogService extends IService<MailReceiveLog> {

    Result getPage(Long userId, Integer current, Integer size);

    Result getById(Long id);

    Result deleteById(Long id);

    Result batchDelete(List<Long> ids);
}
