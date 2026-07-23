package com.intellectual.service.impl;

import com.intellectual.service.ApplicationPackageService;
import com.intellectual.model.entity.ApplicationPackage;
import com.intellectual.mapper.ApplicationPackageMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 申请包表(XML包与五书WORD分条目) 服务实现类
 *
 * @author 陈创
 * @since 2026-07-23 16:59
 */
@Service
public class ApplicationPackageServiceImpl extends ServiceImpl<ApplicationPackageMapper, ApplicationPackage> implements ApplicationPackageService {

}
