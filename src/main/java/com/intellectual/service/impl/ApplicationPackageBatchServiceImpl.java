package com.intellectual.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intellectual.mapper.ApplicationPackageBatchMapper;
import com.intellectual.model.entity.ApplicationPackageBatch;
import com.intellectual.service.ApplicationPackageBatchService;
import org.springframework.stereotype.Service;

@Service
public class ApplicationPackageBatchServiceImpl
        extends ServiceImpl<ApplicationPackageBatchMapper, ApplicationPackageBatch>
        implements ApplicationPackageBatchService {
}
