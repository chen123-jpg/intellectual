package com.intellectual.controller;

import com.intellectual.annotation.RequirePermission;
import com.intellectual.exception.BusinessException;
import com.intellectual.model.constants.Constants;
import com.intellectual.model.constants.ExceptionConstants;
import com.intellectual.model.constants.MessageConstants;
import com.intellectual.model.dto.LoginDto;
import com.intellectual.model.dto.LoginResult;
import com.intellectual.model.dto.RegisterDto;
import com.intellectual.model.dto.Result;
import com.intellectual.redis.RedisUtils;
import com.intellectual.security.LoginUser;
import com.intellectual.service.UserService;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 账户控制器 —— 登录、注册、验证码、退出、当前用户信息
 */
@Slf4j
@RestController
@RequestMapping("api/acount")
public class AccountController {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserService userService;

    /**
     * 生成算术图形验证码
     * <p>返回 Base64 格式图片及验证码 key，key 用于登录/注册时校验</p>
     *
     * @param oldCheckCodeKey 旧的验证码 key（可选，传入后会先删除旧的）
     */
    @GetMapping("/checkCode")
    public Result checkCode(@RequestParam(required = false) String oldCheckCodeKey) {
        if (oldCheckCodeKey != null) {
            redisUtils.del(Constants.REDIS_KEY_CHECK_CODE + oldCheckCodeKey);
        }
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        String checkCodeKey = UUID.randomUUID().toString();
        log.info("验证码: {}, key: {}", code, checkCodeKey);
        // 验证码缓存 1 分钟
        redisUtils.set(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code, Constants.REDIS_TIME_1MIN);

        Map<String, String> result = new HashMap<>();
        result.put("checkCode", captcha.toBase64());
        result.put("checkCodeKey", checkCodeKey);
        return Result.success(result);
    }

    /**
     * 生成手机验证码
     * @param mobile
     * @return
     */
    @GetMapping("/getSmsCode")
    public Result sendSmsCode(@RequestParam String mobile) {
        if(redisUtils.hasKey(Constants.REDIS_MOBILE_CHECK_CODE+mobile)&& redisUtils.getExpire(Constants.REDIS_MOBILE_CHECK_CODE+mobile)>240L) {
            long remainTime = redisUtils.getExpire(Constants.REDIS_MOBILE_CHECK_CODE+mobile);
            String msg = "验证发送过于频繁，请 "+ (remainTime-240) +" 秒后再试！";
            log.info(msg);
            return Result.fail(msg);
        }
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        log.info( "{}的验证码: {}",mobile, code);
        redisUtils.set(Constants.REDIS_MOBILE_CHECK_CODE+mobile, code, Constants.REDIS_TIME_5MIN);
        return Result.success(MessageConstants.GET_SMS_SUCCESS);
    }

    /**
     * 用户注册（公开接口）
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterDto registerDto) {
        log.info("注册用户:账号：{},密码：{},手机号：{}", registerDto.getLoginName(), registerDto.getPassword(), registerDto.getPhoneNumber());
        try {
            validateCheckCode(registerDto.getCheckCodeKey(), registerDto.getCheckCode());
            validateSmsCode(registerDto.getPhoneNumber(),registerDto.getSmsCode());
            userService.register(registerDto);
            log.info("注册成功");
            return Result.success("注册成功");
        } catch (BusinessException e) {
            log.error(e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.fail("注册失败");
        }
    }

    /**
     * 用户登录（公开接口）
     * <p>返回 JWT Token 及用户的角色、权限列表</p>
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginDto loginDto) {
        log.info("用户：{}，开始登录", loginDto.getLoginName());
        try {
            validateCheckCode(loginDto.getCheckCodeKey(), loginDto.getCheckCode());
            validateSmsCode(loginDto.getPhoneNumber(), loginDto.getSmsCode());
            LoginResult loginResult = userService.login(loginDto);
            log.info("登录成功: {}", loginDto.getLoginName());
            return Result.success(loginResult, "登录成功");
        } catch (BusinessException e) {
            log.error("登录失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 退出登录（需认证）
     * <p>删除 Redis 中的 Token 缓存并清空安全上下文</p>
     */
    @PostMapping("/logout")
    public Result logout() {
        LoginUser loginUser = getLoginUser();
        if (loginUser != null) {
            redisUtils.del(Constants.REDIS_KEY_JWT_TOKEN + loginUser.getUserId());
            SecurityContextHolder.clearContext();
        }
        return Result.successMsg("退出成功");
    }

    /**
     * 获取当前登录用户信息（需认证）
     */
    @GetMapping("/me")
    public Result currentUser() {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return Result.fail("未登录", 401);
        }
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", loginUser.getUserId());
        userInfo.put("loginName", loginUser.getLoginName());
        userInfo.put("roles", loginUser.getRoles());
        userInfo.put("email",loginUser.getEmail());
        userInfo.put("permissions", loginUser.getPermissions());
        return Result.success(userInfo);
    }


    //填写邮箱授权码并保存
    @PostMapping("/authCode")
    public Result saveAuthCode(Long userId,String email,String authCode){

        try{
            userService.saveAuthCode(userId, email, authCode);
            return Result.success(null);
        }catch (BusinessException e){
            log.error("保存失败");
            return Result.fail(e.getMessage());
        }catch (Exception e){
            log.error(e.getMessage());
            return Result.fail(null);
        }

    }

    @PostMapping("password")
    public Result changePassword(Long userId,String oldPassword ,String newPassword){
        log.info("更换密码开始，userId:{} ,原始密码：{} ，新密码：{}",userId,oldPassword,newPassword);
        try{
            userService.changePassword(userId,oldPassword,newPassword);
            return Result.success(null);
        }catch (BusinessException e){
            log.error(e.getMessage());
            return Result.fail("修改失败:"+e.getMessage());
        }
        catch (Exception e){
            log.error(e.getMessage());
            return Result.fail(null);
        }
    }

    /**
     * 校验验证码
     *
     * @param checkCodeKey 验证码 key
     * @param checkCode    用户输入的验证码
     */
    private void validateCheckCode(String checkCodeKey, String checkCode) {
        Object storedObj = redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        if (storedObj == null) {
            throw new BusinessException(ExceptionConstants.CODE_NOT_EXITS);
        }
        if (!checkCode.equalsIgnoreCase(storedObj.toString())) {
            throw new BusinessException(ExceptionConstants.CODE_NOT_RIGHT);
        }
    }

    /**
     * 校验手机验证码
     *
     * @param mobile 用户手机号
     * @param checkCode 用户输入的验证码
     */
    private void validateSmsCode(String mobile, String checkCode) {
        if(!redisUtils.hasKey(Constants.REDIS_MOBILE_CHECK_CODE+mobile)) {
            throw new BusinessException(ExceptionConstants.CODE_NOT_EXITS);
        }
        if(!redisUtils.get(Constants.REDIS_MOBILE_CHECK_CODE+mobile).equals(checkCode)) {
            throw new BusinessException(ExceptionConstants.CODE_NOT_RIGHT);
        }
        redisUtils.del(Constants.REDIS_MOBILE_CHECK_CODE+mobile);
    }

    /** 从安全上下文获取当前登录用户，未登录返回 null */
    private LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }
}
