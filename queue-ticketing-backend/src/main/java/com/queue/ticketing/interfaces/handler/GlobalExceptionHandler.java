package com.queue.ticketing.interfaces.handler;

import com.queue.ticketing.common.BizException;
import com.queue.ticketing.common.ErrorCodeEnum;
import com.queue.ticketing.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.queue.ticketing.interfaces")
public class GlobalExceptionHandler {

    /**
     * 业务异常
     *
     * @param request 请求
     * @param e       业务异常
     * @return 统一响应
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(HttpServletRequest request, BizException e) {
        log.warn("业务异常，uri={}，code={}，message={}", request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常（@RequestBody）
     *
     * @param request 请求
     * @param e       校验异常
     * @return 统一响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException e) {
        String firstMessage = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> "参数[" + error.getField() + "]" + error.getDefaultMessage())
                .orElse("参数校验失败");
        log.warn("参数校验失败，uri={}，message={}", request.getRequestURI(), firstMessage);
        return Result.error(ErrorCodeEnum.PARAM_ERROR.getCode(), firstMessage);
    }

    /**
     * 参数绑定异常
     *
     * @param request 请求
     * @param e       绑定异常
     * @return 统一响应
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(HttpServletRequest request, BindException e) {
        String firstMessage = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> "参数[" + error.getField() + "]" + error.getDefaultMessage())
                .orElse("参数绑定失败");
        log.warn("参数绑定失败，uri={}，message={}", request.getRequestURI(), firstMessage);
        return Result.error(ErrorCodeEnum.PARAM_ERROR.getCode(), firstMessage);
    }

    /**
     * 参数校验异常（@RequestParam / @PathVariable）
     *
     * @param request 请求
     * @param e       校验异常
     * @return 统一响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(HttpServletRequest request, ConstraintViolationException e) {
        String firstMessage = e.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("参数校验失败");
        log.warn("参数校验失败，uri={}，message={}", request.getRequestURI(), firstMessage);
        return Result.error(ErrorCodeEnum.PARAM_ERROR.getCode(), firstMessage);
    }

    /**
     * 其他未知异常
     *
     * @param request 请求
     * @param e       异常
     * @return 统一响应
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(HttpServletRequest request, Exception e) {
        log.error("系统异常，uri={}", request.getRequestURI(), e);
        return Result.error(ErrorCodeEnum.SYSTEM_ERROR);
    }
}
