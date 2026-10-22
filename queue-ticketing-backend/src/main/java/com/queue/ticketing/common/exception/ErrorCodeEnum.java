package com.queue.ticketing.common.exception;

import lombok.Getter;

/**
 * 错误码枚举
 *
 * @author yh
 * @date 2026-10-22
 */
@Getter
public enum ErrorCodeEnum {

    /**
     * 成功
     */
    SUCCESS(200, "success"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权"),

    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 请求过于频繁
     */
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    /**
     * 系统内部错误
     */
    SYSTEM_ERROR(500, "系统内部错误"),

    /**
     * 业务错误
     */
    BIZ_ERROR(1000, "业务处理失败"),

    /**
     * 队列不存在
     */
    QUEUE_NOT_FOUND(1001, "队列不存在"),

    /**
     * 队列已暂停
     */
    QUEUE_PAUSED(1002, "队列已暂停"),

    /**
     * 队列已关闭
     */
    QUEUE_CLOSED(1003, "队列已关闭"),

    /**
     * 队列已满
     */
    QUEUE_FULL(1004, "队列已满，请稍后再试"),

    /**
     * 取号单不存在
     */
    TICKET_NOT_FOUND(1100, "取号单不存在"),

    /**
     * 取号单状态非法
     */
    TICKET_STATUS_ILLEGAL(1101, "取号单状态非法"),

    /**
     * 重复取号
     */
    TICKET_DUPLICATE(1102, "您已有进行中的排队号码"),

    /**
     * 叫号失败
     */
    CALL_TICKET_FAILED(1103, "叫号失败，当前队列无等待号码"),

    /**
     * 号码生成失败
     */
    TICKET_NO_GENERATE_FAILED(1104, "号码生成失败"),

    /**
     * 门店不存在
     */
    STORE_NOT_FOUND(1200, "门店不存在"),

    /**
     * 柜台不存在
     */
    COUNTER_NOT_FOUND(1201, "柜台不存在"),

    /**
     * 操作频繁
     */
    OPERATION_TOO_FREQUENT(1300, "操作过于频繁，请稍后再试"),

    /**
     * 幂等请求重复
     */
    IDEMPOTENT_REQUEST(1301, "请求已处理，请勿重复提交");

    private final Integer code;
    private final String message;

    ErrorCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
