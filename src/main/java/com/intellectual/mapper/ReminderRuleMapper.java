package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.ReminderRule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* 提醒规则表 Mapper
*
* @author 陈创
* @since 2026-08-01 17:00
*/
@Mapper
public interface ReminderRuleMapper extends BaseMapper<ReminderRule> {
}
