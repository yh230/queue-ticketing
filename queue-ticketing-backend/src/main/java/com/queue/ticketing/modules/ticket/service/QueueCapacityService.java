package com.queue.ticketing.modules.ticket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.queue.ticketing.common.constant.QueueConstants;
import com.queue.ticketing.modules.queue.entity.QueueInstance;
import com.queue.ticketing.modules.queue.mapper.QueueInstanceMapper;
import com.queue.ticketing.modules.ticket.enums.TicketStatus;
import com.queue.ticketing.modules.ticket.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 队列容量计数器
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueueCapacityService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TicketMapper ticketMapper;
    private final QueueInstanceMapper queueInstanceMapper;

    private static final long KEY_TTL_HOURS = 24L;

    /**
     * 占用一个等待名额
     *
     * @param queueInstanceId 队列实例ID
     * @param capacity        最大容量
     * @return 是否占用成功
     */
    public boolean tryAcquire(Long queueInstanceId, int capacity) {
        String key = buildKey(queueInstanceId);
        Long current = redisTemplate.opsForValue().increment(key);
        if (current == null) {
            log.error("队列容量计数器自增失败，queueInstanceId={}", queueInstanceId);
            return false;
        }

        // 首次创建计数器时用数据库实际等待数初始化
        if (current == 1L) {
            Long dbCount = ticketMapper.countByInstanceAndStatus(
                    queueInstanceId, TicketStatus.WAITING.getCode());
            if (dbCount != null && dbCount > 0) {
                current = dbCount + 1;
                redisTemplate.opsForValue().set(key, current);
            }
            redisTemplate.expire(key, KEY_TTL_HOURS, TimeUnit.HOURS);
        }

        if (current > capacity) {
            redisTemplate.opsForValue().decrement(key);
            return false;
        }
        return true;
    }

    /**
     * 释放一个等待名额
     *
     * @param queueInstanceId 队列实例ID
     */
    public void release(Long queueInstanceId) {
        release(queueInstanceId, 1);
    }

    /**
     * 释放指定数量的等待名额
     *
     * @param queueInstanceId 队列实例ID
     * @param count           释放数量
     */
    public void release(Long queueInstanceId, int count) {
        String key = buildKey(queueInstanceId);
        Long current = redisTemplate.opsForValue().decrement(key, count);
        if (current != null && current < 0) {
            redisTemplate.opsForValue().set(key, 0);
        }
    }

    /**
     * 用数据库实际等待数校正计数器
     *
     * @param queueInstanceId 队列实例ID
     */
    public void reconcile(Long queueInstanceId) {
        String key = buildKey(queueInstanceId);
        Long dbCount = ticketMapper.countByInstanceAndStatus(
                queueInstanceId, TicketStatus.WAITING.getCode());
        redisTemplate.opsForValue().set(key, dbCount == null ? 0 : dbCount, KEY_TTL_HOURS, TimeUnit.HOURS);
    }

    /**
     * 校正当天所有队列实例的计数器
     */
    public void reconcileToday() {
        List<QueueInstance> instances = queueInstanceMapper.selectList(
                new LambdaQueryWrapper<QueueInstance>()
                        .eq(QueueInstance::getQueueDate, LocalDate.now())
        );
        for (QueueInstance instance : instances) {
            reconcile(instance.getId());
        }
        log.debug("队列容量计数器对账完成，实例数={}", instances.size());
    }

    private String buildKey(Long queueInstanceId) {
        return QueueConstants.QUEUE_WAITING_KEY_PREFIX + queueInstanceId;
    }
}
