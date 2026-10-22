package com.queue.ticketing.modules.notify.listener;

import com.queue.ticketing.common.QueueConstants;
import com.queue.ticketing.modules.notify.service.NotifyService;
import com.queue.ticketing.modules.ticket.entity.Ticket;
import com.queue.ticketing.modules.ticket.event.TicketCalledEvent;
import com.queue.ticketing.modules.ticket.event.TicketCancelledEvent;
import com.queue.ticketing.modules.ticket.event.TicketCreatedEvent;
import com.queue.ticketing.modules.ticket.event.TicketMissedEvent;
import com.queue.ticketing.modules.ticket.event.TicketServedEvent;
import com.queue.ticketing.modules.ticket.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * 取号单事件监听器
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketEventListener {

    private final NotifyService notifyService;
    private final TicketMapper ticketMapper;

    /**
     * 取号成功通知
     *
     * @param event 事件
     */
    @Async("eventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketCreated(TicketCreatedEvent event) {
        log.info("监听到取号成功事件，ticketId={}", event.getTicketId());
        notifyService.sendNotify(event.getTicketId(), "WECHAT_MINI", "TICKET_CREATED");
    }

    /**
     * 叫号通知
     *
     * @param event 事件
     */
    @Async("eventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketCalled(TicketCalledEvent event) {
        log.info("监听到叫号事件，ticketId={}", event.getTicketId());
        notifyService.sendNotify(event.getTicketId(), "WECHAT_MINI", "TICKET_CALLED");

        // 前方 N 人提醒
        List<Ticket> waitingTickets = ticketMapper.selectWaitingTicketsOrdered(
                event.getQueueInstanceId(), QueueConstants.DEFAULT_AHEAD_NOTIFY_COUNT);
        for (Ticket ticket : waitingTickets) {
            notifyService.sendNotify(ticket.getId(), "WECHAT_MINI", "TICKET_AHEAD");
        }
    }

    /**
     * 过号通知
     *
     * @param event 事件
     */
    @Async("eventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketMissed(TicketMissedEvent event) {
        log.info("监听到过号事件，ticketId={}", event.getTicketId());
        notifyService.sendNotify(event.getTicketId(), "WECHAT_MINI", "TICKET_MISSED");
    }

    /**
     * 取消通知
     *
     * @param event 事件
     */
    @Async("eventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketCancelled(TicketCancelledEvent event) {
        log.info("监听到取消事件，ticketId={}", event.getTicketId());
    }

    /**
     * 服务完成通知
     *
     * @param event 事件
     */
    @Async("eventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketServed(TicketServedEvent event) {
        log.info("监听到服务完成事件，ticketId={}", event.getTicketId());
    }
}
