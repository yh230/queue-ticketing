package com.queue.ticketing.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 基于 Spring 事件的领域事件发布器
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        log.info("发布领域事件，eventType={}", event.getClass().getSimpleName());
        applicationEventPublisher.publishEvent(event);
    }
}
