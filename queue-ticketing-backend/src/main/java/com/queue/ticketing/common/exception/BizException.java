package com.queue.ticketing.common.exception;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author yh
 * @date 2026-10-22
 */
@Getter
public class BizException extends RuntimeException {

    private final Integer code;

    public BizException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getMessage());
        this.code = errorCodeEnum.getCode();
    }

    public BizException(ErrorCodeEnum errorCodeEnum, String message) {
        super(message);
        this.code = errorCodeEnum.getCode();
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
