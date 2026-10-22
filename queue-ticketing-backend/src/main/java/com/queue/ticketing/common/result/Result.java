package com.queue.ticketing.common.result;

import com.queue.ticketing.common.exception.ErrorCodeEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应包装
 *
 * @param <T> 数据类型
 * @author yh
 * @date 2026-10-22
 */
@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 请求追踪ID
     */
    private String traceId;

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应
     *
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return new Result<>(ErrorCodeEnum.SUCCESS.getCode(), ErrorCodeEnum.SUCCESS.getMessage(), null);
    }

    /**
     * 成功响应
     *
     * @param data 数据
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCodeEnum.SUCCESS.getCode(), ErrorCodeEnum.SUCCESS.getMessage(), data);
    }

    /**
     * 失败响应
     *
     * @param errorCode 错误码
     * @return 失败结果
     */
    public static <T> Result<T> error(ErrorCodeEnum errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 失败响应
     *
     * @param code    状态码
     * @param message 提示信息
     * @return 失败结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败响应
     *
     * @param errorCode 错误码
     * @param message   自定义提示
     * @return 失败结果
     */
    public static <T> Result<T> error(ErrorCodeEnum errorCode, String message) {
        return new Result<>(errorCode.getCode(), message, null);
    }
}
