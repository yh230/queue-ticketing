package com.queue.ticketing.modules.ticket.event;

import com.queue.ticketing.common.event.DomainEvent;
import lombok.Getter;

/**
 * 取号单创建事件
 *
 * @author yh
 * @date 2026-10-22
 */
@Getter
public class TicketCreatedEvent extends DomainEvent {

    private final Long ticketId;
    private final Long queueInstanceId;
    private final String ticketNo;
    private final String userId;

    public TicketCreatedEvent(Long ticketId, Long queueInstanceId, String ticketNo, String userId) {
        this.ticketId = ticketId;
        this.queueInstanceId = queueInstanceId;
        this.ticketNo = ticketNo;
        this.userId = userId;
    }
}
