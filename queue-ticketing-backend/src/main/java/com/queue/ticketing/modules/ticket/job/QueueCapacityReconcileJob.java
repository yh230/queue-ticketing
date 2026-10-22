package com.queue.ticketing.modules.ticket.job;

import com.queue.ticketing.modules.ticket.service.QueueCapacityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 队列容量计数器对账任务
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueueCapacityReconcileJob {

    private final QueueCapacityService queueCapacityService;

    /**
     * 每 5 分钟用数据库实际等待数校正 Redis 计数器
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000L)
    public void reconcile() {
        try {
            queueCapacityService.reconcileToday();
        } catch (Exception e) {
            log.error("队列容量计数器对账失败", e);
        }
    }
}
