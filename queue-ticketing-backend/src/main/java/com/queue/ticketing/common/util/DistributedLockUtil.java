package com.queue.ticketing.common.util;

import com.queue.ticketing.common.exception.BizException;
import com.queue.ticketing.common.exception.ErrorCodeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁工具
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockUtil {

    private final RedissonClient redissonClient;

    /**
     * 尝试获取锁并执行逻辑，执行完成后自动释放锁
     *
     * @param lockKey     锁Key
     * @param waitSeconds 最大等待时间（秒）
     * @param leaseSeconds 锁自动释放时间（秒）
     * @param supplier    业务逻辑
     * @return 业务结果
     */
    public <T> T executeWithLock(String lockKey, long waitSeconds, long leaseSeconds, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(waitSeconds, leaseSeconds, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("获取分布式锁失败，lockKey={}", lockKey);
                throw new BizException(ErrorCodeEnum.OPERATION_TOO_FREQUENT);
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断，lockKey={}", lockKey, e);
            throw new BizException(ErrorCodeEnum.SYSTEM_ERROR, "获取分布式锁失败");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    log.error("释放分布式锁异常，lockKey={}", lockKey, e);
                }
            }
        }
    }

    /**
     * 尝试获取锁并执行无返回值逻辑
     *
     * @param lockKey      锁Key
     * @param waitSeconds  最大等待时间（秒）
     * @param leaseSeconds 锁自动释放时间（秒）
     * @param runnable     业务逻辑
     */
    public void executeWithLock(String lockKey, long waitSeconds, long leaseSeconds, Runnable runnable) {
        executeWithLock(lockKey, waitSeconds, leaseSeconds, () -> {
            runnable.run();
            return null;
        });
    }
}
