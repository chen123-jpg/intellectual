package com.intellectual.service.impl;

import com.intellectual.service.NotificationMessageService;
import com.intellectual.model.entity.NotificationMessage;
import com.intellectual.mapper.NotificationMessageMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 提醒消息表 服务实现类
 *
 * @author 陈创
 * @since 2026-08-01 17:00
 */
@Service
public class NotificationMessageServiceImpl extends ServiceImpl<NotificationMessageMapper, NotificationMessage> implements NotificationMessageService {

}
