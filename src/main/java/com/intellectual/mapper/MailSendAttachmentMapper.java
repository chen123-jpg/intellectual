package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.MailSendAttachment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* 邮件发送附件表 Mapper
*
* @author 陈创
* @since 2026-07-21 17:19
*/
@Mapper
public interface MailSendAttachmentMapper extends BaseMapper<MailSendAttachment> {
}
