package com.chen.intellectualproperty.service.impl;

import com.chen.intellectualproperty.mapper.GenerateNoMapper;
import com.chen.intellectualproperty.service.GenerateNoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GenerateNoServiceImpl implements GenerateNoService {

    @Autowired
    private GenerateNoMapper generateNoMapper;

    // 流水号位数
    private static final int SERIAL_LENGTH = 3;
    // 表名白名单，防止SQL注入
    private static final List<String> TABLE_WHITE_LIST = Arrays.asList(
            "patent_new_application"
            ,"patent_intermediate_changes"
            ,"patent_pcts"
            ,"patent_supplementaries"
            ,"patent_reexaminations"
            ,"patent_disclosure");
    private static final List<String> KEY_WHITE_LIST = Arrays.asList(
            "temp_no"
            ,"internal_no"
            ,"pct_internal_no");
    // 最大重试次数（并发冲突重试）
    private static final int RETRY_COUNT = 3;

    @Override
    public String generateNo(String tableName, String key, String prefix) {
        // 1. 参数校验
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new RuntimeException("编号前缀不能为空");
        }
        String realPrefix = prefix.trim();
        if (!TABLE_WHITE_LIST.contains(tableName)) {
            throw new RuntimeException("不允许操作数据表");
        }
        if (!KEY_WHITE_LIST.contains(key)) {
            throw new RuntimeException("不允许操作");
        }

        String currentYear = String.valueOf(Year.now().getValue());
        String prefixYear = realPrefix + currentYear;

        // 循环重试，防止并发重复编号
        for (int i = 0; i < RETRY_COUNT; i++) {
            String maxNo = generateNoMapper.getMaxBusinessNo(tableName, key, realPrefix, currentYear);
            int serialNum;
            if (maxNo == null) {
                serialNum = 1;
            } else {
                String regex = "^" + Pattern.quote(prefixYear) + "(\\d+)$";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(maxNo);
                if (matcher.find()) {
                    serialNum = Integer.parseInt(matcher.group(1)) + 1;
                } else {
                    serialNum = 1;
                }
            }
            String serialStr = String.format("%0" + SERIAL_LENGTH + "d", serialNum);
            String newNo = prefixYear + serialStr;
            return newNo;
        }
        throw new RuntimeException("编号生成失败，超出最大重试次数");
    }
}