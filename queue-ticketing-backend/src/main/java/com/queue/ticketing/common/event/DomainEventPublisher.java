package com.queue.ticketing.common.event;

/**
 * 领域事件发布器
 *
 * @author yh
 * @date 2026-10-22
 */
public interface DomainEventPublisher {

    /**
     * 发布事件
     *
     * @param event 领域事件
     */
    void publish(DomainEvent event);
}
