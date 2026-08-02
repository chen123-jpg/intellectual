package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.NotificationMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* 提醒消息表 Mapper
*
* @author 陈创
* @since 2026-08-01 17:00
*/
@Mapper
public interface NotificationMessageMapper extends BaseMapper<NotificationMessage> {
}
