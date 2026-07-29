package com.intellectual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellectual.exception.BusinessException;
import com.intellectual.model.entity.Role;
import com.intellectual.model.entity.User;
import com.intellectual.model.entity.UserRole;
import com.intellectual.model.vo.SponsorOptionVo;
import com.intellectual.service.RoleService;
import com.intellectual.service.SponsorDirectoryService;
import com.intellectual.service.UserRoleService;
import com.intellectual.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.intellectual.model.constants.TtableConstant.ROLE_ORGANIZER;

@Service
public class SponsorDirectoryServiceImpl implements SponsorDirectoryService {

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final RoleService roleService;

    public SponsorDirectoryServiceImpl(UserService userService,
                                       UserRoleService userRoleService,
                                       RoleService roleService) {
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.roleService = roleService;
    }

    @Override
    public List<SponsorOptionVo> listActiveSponsors() {
        Role organizerRole = activeOrganizerRole();
        if (organizerRole == null) {
            return List.of();
        }
        List<Long> userIds = userRoleService.list(new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getRoleId, organizerRole.getRoleId())).stream()
                .map(UserRole::getUserId)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return List.of();
        }
        return userService.list(new LambdaQueryWrapper<User>()
                        .in(User::getUserId, userIds)
                        .eq(User::getStatus, "0")
                        .eq(User::getDelFlag, "0")
                        .orderByAsc(User::getUserName)
                        .orderByAsc(User::getLoginName)).stream()
                .map(this::toOption)
                .toList();
    }

    @Override
    public SponsorOptionVo requireActiveSponsor(Long userId) {
        if (userId == null) {
            throw new BusinessException("请选择主办人");
        }
        Role organizerRole = activeOrganizerRole();
        User user = userService.getById(userId);
        boolean activeUser = user != null
                && "0".equals(user.getStatus())
                && "0".equals(user.getDelFlag());
        boolean linked = organizerRole != null && userRoleService.count(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userId)
                        .eq(UserRole::getRoleId, organizerRole.getRoleId())) > 0;
        if (!activeUser || !linked) {
            throw new BusinessException("所选用户不是启用的主办人");
        }
        return toOption(user);
    }

    private Role activeOrganizerRole() {
        return roleService.getOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleKey, ROLE_ORGANIZER)
                .eq(Role::getStatus, "0")
                .eq(Role::getDelFlag, "0")
                .last("LIMIT 1"), false);
    }

    private SponsorOptionVo toOption(User user) {
        String userName = hasText(user.getUserName()) ? user.getUserName() : user.getLoginName();
        return new SponsorOptionVo(user.getUserId(), userName, user.getLoginName());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
