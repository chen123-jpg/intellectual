package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.MailReceiveLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 邮件接收记录表 Mapper
 *
 * @author 陈创
 * @since 2026-08-02
 */
@Mapper
public interface MailReceiveLogMapper extends BaseMapper<MailReceiveLog> {
}
