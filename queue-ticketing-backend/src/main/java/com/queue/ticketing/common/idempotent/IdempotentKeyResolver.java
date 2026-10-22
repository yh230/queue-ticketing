package com.queue.ticketing.common.idempotent;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 幂等键解析器
 *
 * @author yh
 * @date 2026-10-22
 */
public interface IdempotentKeyResolver {

    /**
     * 解析幂等键
     *
     * @param joinPoint 切点
     * @param keyExpr   表达式
     * @return 幂等键
     */
    String resolve(ProceedingJoinPoint joinPoint, String keyExpr);
}
