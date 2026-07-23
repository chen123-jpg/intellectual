package com.intellectual.service.impl;

import com.intellectual.service.ApplicantService;
import com.intellectual.model.entity.Applicant;
import com.intellectual.mapper.ApplicantMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 申请人表 服务实现类
 *
 * @author 陈创
 * @since 2026-07-23 16:59
 */
@Service
public class ApplicantServiceImpl extends ServiceImpl<ApplicantMapper, Applicant> implements ApplicantService {

}
