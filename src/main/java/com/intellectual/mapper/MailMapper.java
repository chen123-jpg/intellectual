package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.Mail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* 用户邮箱表 Mapper
*
* @author 陈创
* @since 2026-07-23 19:09
*/
@Mapper
public interface MailMapper extends BaseMapper<Mail> {
}
