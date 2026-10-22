package com.queue.ticketing.modules.ticket.event;

import com.queue.ticketing.common.event.DomainEvent;
import lombok.Getter;

/**
 * 取号单服务完成事件
 *
 * @author yh
 * @date 2026-10-22
 */
@Getter
public class TicketServedEvent extends DomainEvent {

    private final Long ticketId;
    private final Long queueInstanceId;
    private final String ticketNo;
    private final String userId;
    private final Long counterId;

    public TicketServedEvent(Long ticketId, Long queueInstanceId, String ticketNo, String userId, Long counterId) {
        this.ticketId = ticketId;
        this.queueInstanceId = queueInstanceId;
        this.ticketNo = ticketNo;
        this.userId = userId;
        this.counterId = counterId;
    }
}
