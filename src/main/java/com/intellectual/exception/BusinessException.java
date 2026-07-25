package com.intellectual.exception;

import com.intellectual.model.enums.ResponseCodeEnum;

/**
 * 全局业务自定义异常
 */
public class BusinessException extends RuntimeException {

    private final Integer code;

    /**
     * 业务异常（默认600码）
     * @param message 异常信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 业务异常（指定响应码）
     * @param code 响应码
     * @param message 异常信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 业务异常（使用响应码枚举）
     * @param responseCode 响应码枚举
     */
    public BusinessException(ResponseCodeEnum responseCode) {
        super(responseCode.getMsg());
        this.code = responseCode.getCode();
    }

    /**
     * 业务异常（使用响应码枚举，自定义消息）
     * @param responseCode 响应码枚举
     * @param message 自定义异常信息
     */
    public BusinessException(ResponseCodeEnum responseCode, String message) {
        super(message != null && !message.isEmpty() ? message : responseCode.getMsg());
        this.code = responseCode.getCode();
    }

    public Integer getCode() {
        return code;
    }
}
