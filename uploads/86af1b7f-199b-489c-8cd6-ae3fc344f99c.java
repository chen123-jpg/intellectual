package com.chen.intellectualproperty.service.impl;

import com.chen.intellectualproperty.service.PatentPctService;
import com.chen.intellectualproperty.mapper.PatentPctMapper;
import com.chen.intellectualproperty.model.entity.PatentPct;
import com.chen.intellectualproperty.model.query.PatentPctQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * PCT国际申请表 ServiceImpl
 *
 * @author
 */
@Service
public class PatentPctServiceImpl implements PatentPctService {

    @Autowired
    private PatentPctMapper patentPctMapper;

    @Override
    @Transactional
    public int insert(PatentPct record) {
        return patentPctMapper.insert(record);
    }

    @Override
    @Transactional
    public int insertOrUpdate(PatentPct record) {
        return patentPctMapper.insertOrUpdate(record);
    }

    @Override
    @Transactional
    public int deleteById(Long id) {
        return patentPctMapper.deleteById(id);
    }

    @Override
    @Transactional
    public int updateById(PatentPct record) {
        return patentPctMapper.updateById(record);
    }

    @Override
    public PatentPct selectById(Long id) {
        return patentPctMapper.selectById(id);
    }

    @Override
    public List<PatentPct> selectAll() {
        return patentPctMapper.selectAll();
    }

    @Override
    public List<PatentPct> selectPage(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return patentPctMapper.selectPage(offset, pageSize);
    }

    @Override
    public long count() {
        return patentPctMapper.count();
    }

    @Override
    public List<PatentPct> findListByParam(PatentPctQuery param) {
        return patentPctMapper.findListByParam(param);
    }

    @Override
    public List<PatentPct> findListByParams(Map<String, Object> params) {
        return patentPctMapper.findListByParams(params);
    }

    @Override
    public long selectCount(PatentPctQuery param) {
        return patentPctMapper.selectCount(param);
    }

}
