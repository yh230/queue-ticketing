package com.queue.ticketing.common.idempotent;

import com.queue.ticketing.common.BizException;
import com.queue.ticketing.common.ErrorCodeEnum;
import com.queue.ticketing.common.QueueConstants;
import com.queue.ticketing.common.annotation.Idempotent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 幂等切面
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    private final IdempotentKeyResolver idempotentKeyResolver;

    /**
     * 幂等处理
     *
     * @param joinPoint  切点
     * @param idempotent 幂等注解
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        String key = buildKey(joinPoint, idempotent);
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", idempotent.ttlSeconds(), TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(success)) {
            log.warn("幂等请求重复，key={}", key);
            throw new BizException(ErrorCodeEnum.IDEMPOTENT_REQUEST, idempotent.message());
        }
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            // 业务异常时删除幂等Key，允许用户重试；删除失败仅记录日志
            try {
                redisTemplate.delete(key);
            } catch (Exception deleteException) {
                log.error("幂等Key删除失败，key={}", key, deleteException);
            }
            throw e;
        }
    }

    private String buildKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        String rawKey = idempotentKeyResolver.resolve(joinPoint, idempotent.prefix());
        return QueueConstants.IDEMPOTENCY_KEY_PREFIX + (idempotent.prefix().isBlank() ? "" : idempotent.prefix() + ":") + rawKey;
    }
}
