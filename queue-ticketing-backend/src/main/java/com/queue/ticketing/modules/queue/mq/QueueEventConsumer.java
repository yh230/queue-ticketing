package com.queue.ticketing.modules.queue.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.queue.ticketing.modules.queue.entity.QueueInstance;
import com.queue.ticketing.modules.queue.mapper.QueueInstanceMapper;
import com.queue.ticketing.modules.queue.service.QueueInstanceService;
import com.queue.ticketing.modules.ticket.entity.Ticket;
import com.queue.ticketing.modules.ticket.enums.TicketStatus;
import com.queue.ticketing.modules.ticket.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排队事件消费者：异步落库
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueueEventConsumer {

    private final ObjectMapper objectMapper;
    private final TicketMapper ticketMapper;
    private final QueueInstanceService queueInstanceService;
    private final QueueInstanceMapper queueInstanceMapper;

    /**
     * 消费排队事件
     *
     * @param payload 消息内容
     */
    @KafkaListener(topics = QueueEventProducer.TOPIC, groupId = "queue-persist")
    public void onMessage(String payload) {
        QueueEventMessage message;
        try {
            message = objectMapper.readValue(payload, QueueEventMessage.class);
        } catch (Exception e) {
            log.error("解析排队事件失败，payload={}", payload, e);
            return;
        }

        log.info("消费排队事件，messageId={}，eventType={}", message.getMessageId(), message.getEventType());
        switch (message.getEventType()) {
            case QueueEventMessage.EVENT_TAKE_SUCCESS -> handleTakeSuccess(message);
            case QueueEventMessage.EVENT_CALLED -> handleCalled(message);
            case QueueEventMessage.EVENT_CANCELLED -> handleStatusChange(message, TicketStatus.CANCELLED);
            case QueueEventMessage.EVENT_FINISHED -> handleStatusChange(message, TicketStatus.SERVED);
            case QueueEventMessage.EVENT_MISSED -> handleStatusChange(message, TicketStatus.MISSED);
            default -> log.warn("未知排队事件类型，type={}", message.getEventType());
        }
    }

    private void handleTakeSuccess(QueueEventMessage message) {
        QueueInstance instance = queueInstanceService.getOrCreateInstance(
                message.getStoreId(), message.getTemplateId(), LocalDate.now());

        Ticket ticket = new Ticket();
        ticket.setStoreId(message.getStoreId());
        ticket.setQueueInstanceId(instance.getId());
        ticket.setTicketNo(message.getTicketNoText());
        ticket.setUserId(message.getUid());
        ticket.setUserPhone(message.getUserPhone());
        ticket.setStatus(TicketStatus.WAITING.getCode());
        ticket.setSource(message.getSource());
        ticket.setMissedCount(0);
        ticket.setMessageId(message.getMessageId());
        try {
            ticketMapper.insert(ticket);
            queueInstanceMapper.updateCurrentTicket(instance.getId(), null, 1);
        } catch (DuplicateKeyException e) {
            log.warn("取号消息重复消费，messageId={}", message.getMessageId());
        }
    }

    private void handleCalled(QueueEventMessage message) {
        Ticket ticket = findTicket(message);
        if (ticket == null) {
            throw new IllegalStateException("叫号事件对应取号单不存在：" + message.getTicketNoText());
        }
        ticket.setStatus(TicketStatus.CALLED.getCode());
        ticket.setCounterId(message.getCounterId());
        ticket.setCalledAt(LocalDateTime.now());
        ticketMapper.updateById(ticket);

        QueueInstance instance = queueInstanceService.getOrCreateInstance(
                message.getStoreId(), message.getTemplateId(), LocalDate.now());
        queueInstanceMapper.updateCurrentTicket(instance.getId(), ticket.getId(), -1);
    }

    private void handleStatusChange(QueueEventMessage message, TicketStatus status) {
        Ticket ticket = findTicket(message);
        if (ticket == null) {
            throw new IllegalStateException("状态事件对应取号单不存在：" + message.getTicketNoText());
        }
        ticket.setStatus(status.getCode());
        if (status == TicketStatus.SERVED) {
            ticket.setServedAt(LocalDateTime.now());
        }
        ticketMapper.updateById(ticket);

        if (status == TicketStatus.CANCELLED || status == TicketStatus.SERVED) {
            QueueInstance instance = queueInstanceService.getOrCreateInstance(
                    message.getStoreId(), message.getTemplateId(), LocalDate.now());
            queueInstanceMapper.clearCurrentTicket(instance.getId());
        }
    }

    private Ticket findTicket(QueueEventMessage message) {
        return ticketMapper.selectOne(new LambdaQueryWrapper<Ticket>()
                .eq(Ticket::getStoreId, message.getStoreId())
                .eq(Ticket::getTicketNo, message.getTicketNoText())
                .eq(Ticket::getDeleted, 0)
                .last("LIMIT 1"));
    }
}
