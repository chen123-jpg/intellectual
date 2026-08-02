package com.intellectual.controller;

import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.NotificationMessage;
import com.intellectual.security.LoginUser;
import com.intellectual.service.NotificationMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationMessageService notificationMessageService;

    /** 标记单条消息已读（ACK），防止下次上线重复推送 */
    @PostMapping("/read/{msgId}")
    public Result markRead(@PathVariable Long msgId) {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return Result.fail("未登录", 401);
        }
        NotificationMessage msg = notificationMessageService.getById(msgId);
        if (msg == null) {
            return Result.fail("消息不存在");
        }
        if (!msg.getUserId().equals(loginUser.getUserId())) {
            return Result.fail("无权操作");
        }
        if (msg.getIsRead() == 0) {
            msg.setIsRead(1);
            msg.setReadTime(new Date());
            notificationMessageService.updateById(msg);
        }
        return Result.success(null);
    }

    /** 一键全部已读 */
    @PostMapping("/readAll")
    public Result markAllRead() {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return Result.fail("未登录", 401);
        }
        notificationMessageService.lambdaUpdate()
                .eq(NotificationMessage::getUserId, loginUser.getUserId())
                .eq(NotificationMessage::getIsRead, 0)
                .set(NotificationMessage::getIsRead, 1)
                .set(NotificationMessage::getReadTime, new Date())
                .update();
        return Result.success(null);
    }

    private LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }
}
