package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.MailSendLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* 邮件发送记录表 Mapper
*
* @author 陈创
* @since 2026-07-23 19:09
*/
@Mapper
public interface MailSendLogMapper extends BaseMapper<MailSendLog> {
}
