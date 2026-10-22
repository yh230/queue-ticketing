package com.queue.ticketing.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要登录认证
 *
 * @author yh
 * @date 2026-10-22
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAuth {

    /**
     * 是否允许 C 端用户访问
     *
     * @return 是否允许
     */
    boolean allowClient() default false;
}
