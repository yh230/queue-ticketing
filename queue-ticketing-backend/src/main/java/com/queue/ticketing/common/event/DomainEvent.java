package com.queue.ticketing.common.event;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 领域事件基类
 *
 * @author yh
 * @date 2026-10-22
 */
public abstract class DomainEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 事件发生时间
     */
    private final LocalDateTime occurredOn;

    protected DomainEvent() {
        this.occurredOn = LocalDateTime.now();
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
