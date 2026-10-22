package com.queue.ticketing.common.ratelimit;

import com.queue.ticketing.common.annotation.RateLimit;
import com.queue.ticketing.common.constant.QueueConstants;
import com.queue.ticketing.common.exception.BizException;
import com.queue.ticketing.common.exception.ErrorCodeEnum;
import com.queue.ticketing.modules.auth.context.LoginContext;
import com.queue.ticketing.modules.auth.dto.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 接口限流切面
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 固定窗口限流
     *
     * @param joinPoint 切点
     * @param rateLimit 限流注解
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = QueueConstants.TAKE_RATE_LIMIT_KEY_PREFIX
                + rateLimit.key() + ":" + resolveUserKey();
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, rateLimit.windowSeconds(), TimeUnit.SECONDS);
            }
            if (count != null && count > rateLimit.limit()) {
                throw new BizException(ErrorCodeEnum.TOO_MANY_REQUESTS, rateLimit.message());
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            // Redis 异常时降级放行，避免限流组件影响主流程
            log.warn("限流检查失败，降级放行，key={}", key, e);
        }
        return joinPoint.proceed();
    }

    private String resolveUserKey() {
        LoginUser loginUser = LoginContext.get();
        if (loginUser == null || loginUser.getUserId() == null) {
            return "anonymous";
        }
        return loginUser.getUserId();
    }
}
