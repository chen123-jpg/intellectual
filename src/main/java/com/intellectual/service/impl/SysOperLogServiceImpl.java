package com.intellectual.service.impl;

import com.intellectual.service.SysOperLogService;
import com.intellectual.model.entity.SysOperLog;
import com.intellectual.mapper.SysOperLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 操作日志表 服务实现类
 *
 * @author 陈创
 * @since 2026-08-01 17:00
 */
@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {

}
