package com.intellectual.service.impl;

import com.intellectual.service.PatentReexaminationService;
import com.intellectual.model.entity.PatentReexamination;
import com.intellectual.mapper.PatentReexaminationMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 复审无效专利表 服务实现类
 *
 * @author 陈创
 * @since 2026-07-25 18:12
 */
@Service
public class PatentReexaminationServiceImpl extends ServiceImpl<PatentReexaminationMapper, PatentReexamination> implements PatentReexaminationService {

}
