package com.intellectual.controller;

import com.intellectual.exception.BusinessException;
import com.intellectual.model.constants.Constants;
import com.intellectual.model.dto.LoginDto;
import com.intellectual.model.dto.RegisterDto;
import com.intellectual.model.dto.Result;
import com.intellectual.redis.RedisUtils;
import com.intellectual.service.MailService;
import com.intellectual.service.UserService;
import com.intellectual.utils.PasswordUtils;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/acount")
public class AccountController {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordUtils passwordUtils;

    /**
     * 生成图形验证码
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
        redisUtils.set(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code, Constants.REDIS_TIME_1MIN);

        Map<String, String> result = new HashMap<>();
        result.put("checkCode", captcha.toBase64());
        result.put("checkCodeKey", checkCodeKey);
        return Result.success(result);
    }

    @PostMapping("/register")
    public Result register(@RequestBody RegisterDto registerDto){
        log.info("注册用户:账号：{},密码：{},手机号：{}",registerDto.getLoginName(),registerDto.getPassword(),registerDto.getPhoneNumber());
        try{
            validateCheckCode(registerDto.getCheckCodeKey(),registerDto.getCheckCode());
            userService.register(registerDto);
            log.info("注册成功");
            return Result.success("注册成功");
        }catch (BusinessException e){
            log.error(e.getMessage());
            return Result.fail("注册失败:"+e.getMessage(),e.getCode());
        }catch (Exception e){
            log.error(e.getMessage());
            return Result.fail("注册失败");
        }
    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginDto loginDto){
        log.info("用户：{}，开始登录",loginDto.getLoginName());
        try{
            validateCheckCode(loginDto.getCheckCodeKey(),loginDto.getCheckCode());
            userService.login(loginDto);
            return Result.success("登录成功");
        }catch (BusinessException e){

        }
    }

//    * @param checkCodeKey 验证码key
//     * @param checkCode 用户输入的验证码
    private void validateCheckCode(String checkCodeKey, String checkCode) {
        Object storedObj = redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        if (storedObj == null) {
            throw new BusinessException("验证码不存在或已过期");
        }
        if (!checkCode.equalsIgnoreCase(storedObj.toString())) {
            throw new BusinessException("验证码不匹配");
        }
    }
}
