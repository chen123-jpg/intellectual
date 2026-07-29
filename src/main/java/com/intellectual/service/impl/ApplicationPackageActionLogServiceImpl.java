package com.intellectual.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intellectual.mapper.ApplicationPackageActionLogMapper;
import com.intellectual.model.entity.ApplicationPackageActionLog;
import com.intellectual.service.ApplicationPackageActionLogService;
import org.springframework.stereotype.Service;

@Service
public class ApplicationPackageActionLogServiceImpl
        extends ServiceImpl<ApplicationPackageActionLogMapper, ApplicationPackageActionLog>
        implements ApplicationPackageActionLogService {
}
