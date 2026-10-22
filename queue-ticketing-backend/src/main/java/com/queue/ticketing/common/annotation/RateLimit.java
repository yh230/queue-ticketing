package com.queue.ticketing.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流
 *
 * @author yh
 * @date 2026-10-22
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流业务标识
     *
     * @return 标识
     */
    String key() default "";

    /**
     * 窗口内最大请求数
     *
     * @return 最大请求数
     */
    int limit() default 10;

    /**
     * 窗口大小（秒）
     *
     * @return 窗口秒数
     */
    int windowSeconds() default 60;

    /**
     * 超限提示
     *
     * @return 提示信息
     */
    String message() default "操作过于频繁，请稍后再试";
}
