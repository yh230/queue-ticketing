package com.queue.ticketing.modules.ticket.event;

import com.queue.ticketing.common.event.DomainEvent;
import lombok.Getter;

/**
 * 取号单过号事件
 *
 * @author yh
 * @date 2026-10-22
 */
@Getter
public class TicketMissedEvent extends DomainEvent {

    private final Long ticketId;
    private final Long queueInstanceId;
    private final String ticketNo;
    private final String userId;
    private final Integer missedCount;

    public TicketMissedEvent(Long ticketId, Long queueInstanceId, String ticketNo, String userId, Integer missedCount) {
        this.ticketId = ticketId;
        this.queueInstanceId = queueInstanceId;
        this.ticketNo = ticketNo;
        this.userId = userId;
        this.missedCount = missedCount;
    }
}
