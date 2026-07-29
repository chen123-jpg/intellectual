package com.intellectual.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.intellectual.exception.BusinessException;
import com.intellectual.mapper.MailMapper;
import com.intellectual.mapper.RoleMapper;
import com.intellectual.mapper.UserMapper;
import com.intellectual.mapper.UserRoleMapper;
import com.intellectual.model.dto.UserExcelDto;
import com.intellectual.model.entity.Mail;
import com.intellectual.model.entity.Role;
import com.intellectual.model.entity.User;
import com.intellectual.model.enums.MailServerConfig;
import com.intellectual.service.UserImportService;
import com.intellectual.utils.PasswordUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserImportServiceImpl implements UserImportService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final MailMapper mailMapper;

    public UserImportServiceImpl(UserMapper userMapper, RoleMapper roleMapper, UserRoleMapper userRoleMapper, MailMapper mailMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.mailMapper = mailMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String importUsers(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException("请上传Excel文件(.xlsx或.xls)");
        }

        // 预加载角色映射
        Map<String, Long> roleNameToId = roleMapper.selectList(Wrappers.emptyWrapper())
                .stream()
                .collect(Collectors.toMap(Role::getRoleName, Role::getRoleId, (a, b) -> a));

        // 预加载已存在的用户名集合
        Set<String> existingLoginNames = userMapper.selectList(Wrappers.emptyWrapper())
                .stream()
                .map(User::getLoginName)
                .collect(Collectors.toSet());

        // 读取Excel
        List<UserExcelDto> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(UserExcelDto.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            throw new BusinessException("读取Excel文件失败");
        }

        if (dataList.isEmpty()) {
            throw new BusinessException("Excel文件中没有数据");
        }

        int successCount = 0;
        int skipCount = 0;
        List<String> errors = new ArrayList<>();

        for (UserExcelDto dto : dataList) {
            if (dto.getLoginName() == null || dto.getLoginName().isBlank()) {
                errors.add("存在用户名为空的数据，跳过");
                skipCount++;
                continue;
            }

            if (existingLoginNames.contains(dto.getLoginName())) {
                errors.add("用户名[" + dto.getLoginName() + "]已存在，跳过");
                skipCount++;
                continue;
            }

            Long roleId = roleNameToId.get(dto.getRoleName());
            if (roleId == null) {
                errors.add("用户[" + dto.getLoginName() + "]的角色[" + dto.getRoleName() + "]不存在，跳过");
                skipCount++;
                continue;
            }

            // 创建用户
            User user = new User();
            user.setLoginName(dto.getLoginName());
            user.setPhoneNumber(dto.getPhoneNumber());
            user.setEmail(dto.getEmail());
            user.setPassword(PasswordUtils.encode("123456"));
            user.setStatus("0");
            user.setDelFlag("0");
            user.setUserType("00");
            userMapper.insert(user);

            //绑定邮箱
            Mail mail = new Mail();
            mail.setUserId(user.getUserId());
            mail.setEmail(dto.getEmail());
            MailServerConfig mailServerConfig = MailServerConfig.fromEmail(dto.getEmail());
            mail.setSmtpPort(mailServerConfig.getPort());
            mail.setSmtpHost(mailServerConfig.getHost());
            mailMapper.insert(mail);

            // 绑定角色
            userRoleMapper.insertUserRole(user.getUserId(), roleId);

            existingLoginNames.add(dto.getLoginName());
            successCount++;
        }

        String result = "导入完成：成功" + successCount + "条，跳过" + skipCount + "条";
        if (!errors.isEmpty()) {
            result += "。详细信息：" + String.join("；", errors);
        }
        return result;
    }
}
