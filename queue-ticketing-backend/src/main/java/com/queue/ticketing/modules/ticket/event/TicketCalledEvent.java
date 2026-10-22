package com.queue.ticketing.modules.ticket.event;

import com.queue.ticketing.common.event.DomainEvent;
import lombok.Getter;

/**
 * 取号单叫号事件
 *
 * @author yh
 * @date 2026-10-22
 */
@Getter
public class TicketCalledEvent extends DomainEvent {

    private final Long ticketId;
    private final Long queueInstanceId;
    private final String ticketNo;
    private final String userId;
    private final Long counterId;

    public TicketCalledEvent(Long ticketId, Long queueInstanceId, String ticketNo, String userId, Long counterId) {
        this.ticketId = ticketId;
        this.queueInstanceId = queueInstanceId;
        this.ticketNo = ticketNo;
        this.userId = userId;
        this.counterId = counterId;
    }
}
