package com.intellectual.service.impl;

import com.intellectual.service.PatentIntermediateChangeService;
import com.intellectual.model.entity.PatentIntermediateChange;
import com.intellectual.mapper.PatentIntermediateChangeMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 中间著变专利表（有重复） 服务实现类
 *
 * @author 陈创
 * @since 2026-07-25 18:12
 */
@Service
public class PatentIntermediateChangeServiceImpl extends ServiceImpl<PatentIntermediateChangeMapper, PatentIntermediateChange> implements PatentIntermediateChangeService {

}
