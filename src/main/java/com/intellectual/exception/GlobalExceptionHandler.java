package com.intellectual.exception;

import com.intellectual.model.enums.ResponseCodeEnum;
import com.intellectual.model.dto.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * <p>统一处理Controller层抛出的异常，返回标准化的JSON响应（Result格式）</p>
 * <p>所有异常都会被捕获并转换为 Result 对象返回给前端</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * <p>捕获BusinessException，返回对应的错误码和错误信息</p>
     *
     * @param e 业务异常对象
     * @param request HTTP请求对象
     * @return 标准化的错误响应对象（Result格式）
     */
    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 - URI: {}, ExceptionType: {}, Message: {}, Code: {}", 
                request.getRequestURI(), e.getClass().getSimpleName(), e.getMessage(), e.getCode());
        
        // 如果异常中有自定义错误码，使用它；否则使用默认的600
        Integer code = e.getCode() != null ? e.getCode() : ResponseCodeEnum.CODE_600.getCode();
        String message = e.getMessage() != null ? e.getMessage() : ResponseCodeEnum.CODE_600.getMsg();
        
        return Result.fail(message, code);
    }

    /**
     * 处理参数校验异常（@Valid/@Validated）
     * <p>捕获MethodArgumentNotValidException，返回字段级别的错误信息</p>
     *
     * @param e 参数校验异常对象
     * @param request HTTP请求对象
     * @return 标准化的错误响应对象（Result格式）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("参数校验异常 - URI: {}", request.getRequestURI());
        
        // 收集所有字段错误信息
        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        });
        
        log.warn("字段错误详情: {}", fieldErrors);
        
        // 获取第一个错误信息
        String firstError = fieldErrors.values().iterator().next();
        String message = "参数校验失败: " + firstError;
        
        return Result.fail(message, ResponseCodeEnum.CODE_600.getCode());
    }

    /**
     * 处理绑定异常
     * <p>捕获BindException，返回参数绑定错误信息</p>
     *
     * @param e 绑定异常对象
     * @param request HTTP请求对象
     * @return 标准化的错误响应对象（Result格式）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleBindException(BindException e, HttpServletRequest request) {
        log.warn("参数绑定异常 - URI: {}", request.getRequestURI());
        
        StringBuilder errorMsg = new StringBuilder();
        e.getBindingResult().getAllErrors().forEach(error -> {
            errorMsg.append(error.getDefaultMessage()).append("; ");
        });
        
        // 移除最后的分号和空格
        if (errorMsg.length() > 0) {
            errorMsg.setLength(errorMsg.length() - 2);
        }
        
        return Result.fail(errorMsg.toString(), ResponseCodeEnum.CODE_600.getCode());
    }

    /**
     * 处理请求方法不支持异常
     * <p>捕获HttpRequestMethodNotSupportedException，返回405错误</p>
     *
     * @param e 异常对象
     * @param request HTTP请求对象
     * @return 标准化的错误响应对象（Result格式）
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方法不支持 - URI: {}, Method: {}", request.getRequestURI(), request.getMethod());
        
        String message = "请求方法不支持: " + e.getMethod();
        return Result.fail(message, HttpStatus.METHOD_NOT_ALLOWED.value());
    }

    /**
     * 处理资源未找到异常
     * <p>捕获NoHandlerFoundException，返回404错误</p>
     *
     * @param e 异常对象
     * @param request HTTP请求对象
     * @return 标准化的错误响应对象（Result格式）
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("资源未找到 - URI: {}, Method: {}", request.getRequestURI(), request.getMethod());

        String message = "请求的资源不存在: " + e.getRequestURL();
        return Result.fail(message, HttpStatus.NOT_FOUND.value());
    }

    /**
     * 处理静态资源未找到异常
     * <p>捕获NoResourceFoundException（如缺少favicon.ico），只记录WARN级别日志，不打印堆栈</p>
     *
     * @param e 异常对象
     * @param request HTTP请求对象
     * @return 标准化的错误响应对象（Result格式）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        log.warn("静态资源未找到 - URI: {}, Message: {}", request.getRequestURI(), e.getMessage());
        return Result.fail("资源不存在", HttpStatus.NOT_FOUND.value());
    }

    /**
     * 处理缺少请求参数异常
     * <p>捕获MissingServletRequestParameterException，返回400错误</p>
     *
     * @param e 异常对象
     * @param request HTTP请求对象
     * @return 标准化的错误响应对象（Result格式）
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("缺少请求参数 - URI: {}, Parameter: {}", request.getRequestURI(), e.getParameterName());
        
        String message = "缺少必填参数: " + e.getParameterName();
        return Result.fail(message, ResponseCodeEnum.CODE_600.getCode());
    }

    /**
     * 处理缺少路径变量异常
     * <p>捕获MissingPathVariableException，返回400错误</p>
     *
     * @param e 异常对象
     * @param request HTTP请求对象
     * @return 标准化的错误响应对象（Result格式）
     */
    @ExceptionHandler(MissingPathVariableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request) {
        log.warn("缺少路径变量 - URI: {}, Variable: {}", request.getRequestURI(), e.getVariableName());
        
        String message = "缺少路径变量: " + e.getVariableName();
        return Result.fail(message, ResponseCodeEnum.CODE_600.getCode());
    }

    /**
     * 处理方法参数类型不匹配异常
     * <p>捕获MethodArgumentTypeMismatchException，返回400错误</p>
     *
     * @param e 异常对象
     * @param request HTTP请求对象
     * @return 标准化的错误响应对象（Result格式）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("参数类型不匹配 - URI: {}, Parameter: {}, RequiredType: {}", 
                request.getRequestURI(), e.getName(), e.getRequiredType());
        
        String message = "参数类型错误: " + e.getName();
        return Result.fail(message, ResponseCodeEnum.CODE_600.getCode());
    }

    /**
     * 处理非法参数异常
     * <p>捕获IllegalArgumentException，返回400错误</p>
     *
     * @param e 异常对象
     * @param request HTTP请求对象
     * @return 标准化的错误响应对象（Result格式）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数异常 - URI: {}, Message: {}", request.getRequestURI(), e.getMessage());
        
        String message = e.getMessage() != null ? e.getMessage() : "参数不合法";
        return Result.fail(message, ResponseCodeEnum.CODE_600.getCode());
    }

    /**
     * 处理空指针异常
     * <p>捕获NullPointerException，返回500错误</p>
     *
     * @param e 异常对象
     * @param request HTTP请求对象
     * @return 标准化的错误响应对象（Result格式）
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常 - URI: {}, ExceptionType: {}", 
                request.getRequestURI(), e.getClass().getSimpleName());
        log.error("异常堆栈:", e);
        
        return Result.fail("系统内部错误", ResponseCodeEnum.CODE_500.getCode());
    }

    /**
     * 处理其他未捕获的异常
     * <p>捕获所有Exception，返回500错误</p>
     *
     * @param e 异常对象
     * @param request HTTP请求对象
     * @return 标准化的错误响应对象（Result格式）
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 - URI: {}, ExceptionType: {}, Message: {}", 
                request.getRequestURI(), e.getClass().getSimpleName(), e.getMessage());
        log.error("异常堆栈:", e);
        
        return Result.fail("系统内部错误", ResponseCodeEnum.CODE_500.getCode());
    }
}
