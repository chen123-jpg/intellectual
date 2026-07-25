package com.intellectual.service.impl;

import com.intellectual.service.AgentService;
import com.intellectual.model.entity.Agent;
import com.intellectual.mapper.AgentMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 代理人表 服务实现类
 *
 * @author 陈创
 * @since 2026-07-26 00:42
 */
@Service
public class AgentServiceImpl extends ServiceImpl<AgentMapper, Agent> implements AgentService {

}
