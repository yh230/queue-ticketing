package com.queue.ticketing.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 幂等注解
 *
 * @author yh
 * @date 2026-10-22
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * 幂等Key前缀
     *
     * @return 前缀
     */
    String prefix() default "";

    /**
     * 幂等Key过期时间（秒）
     *
     * @return 过期时间
     */
    long ttlSeconds() default 300L;

    /**
     * 提示信息
     *
     * @return 提示信息
     */
    String message() default "请求已处理，请勿重复提交";
}
