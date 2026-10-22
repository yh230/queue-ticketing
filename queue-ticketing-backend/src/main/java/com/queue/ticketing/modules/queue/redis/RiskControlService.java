package com.queue.ticketing.modules.queue.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 取号风控
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskControlService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final int LIMIT_PER_MINUTE = 5;
    private static final int WINDOW_SECONDS = 60;

    /**
     * 用户取号频控
     *
     * @param uid 用户ID
     * @return 是否允许
     */
    public boolean allowTake(String uid) {
        String key = "risk:take:" + uid;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
        }
        if (count != null && count > LIMIT_PER_MINUTE) {
            log.warn("用户取号触发风控，uid={}，count={}", uid, count);
            return false;
        }
        return true;
    }
}
