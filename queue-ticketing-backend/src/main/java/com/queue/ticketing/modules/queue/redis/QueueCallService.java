package com.queue.ticketing.modules.queue.redis;

import com.queue.ticketing.common.exception.BizException;
import com.queue.ticketing.common.exception.ErrorCodeEnum;
import com.queue.ticketing.modules.queue.mq.QueueEventMessage;
import com.queue.ticketing.modules.queue.mq.QueueEventProducer;
import com.queue.ticketing.modules.ticket.enums.TicketStatus;
import com.queue.ticketing.modules.ticket.vo.TicketVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Redis 叫号服务
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueueCallService {

    private final QueueConfigCache queueConfigCache;
    private final RedisQueueService redisQueueService;
    private final QueueEventProducer producer;

    /**
     * 叫号
     *
     * @param storeId    门店ID
     * @param templateId 队列模板ID
     * @param counterId  柜台ID
     * @return 被叫号码
     */
    public TicketVO call(Long storeId, Long templateId, Long counterId) {
        QueueConfig config = getConfig(storeId, templateId);
        List<?> result = redisQueueService.call(storeId, templateId);
        if (toLong(result.get(0)) != 1L) {
            throw new BizException(ErrorCodeEnum.CALL_TICKET_FAILED, mapMessage(String.valueOf(result.get(3))));
        }

        long ticketNo = toLong(result.get(1));
        String uid = String.valueOf(result.get(3));
        String ticketNoText = QueueTakeService.formatTicketNo(config.getPrefix(), ticketNo);

        sendEvent(QueueEventMessage.EVENT_CALLED, storeId, templateId, uid, ticketNo, ticketNoText, counterId);
        redisQueueService.clearActiveTicketNo(uid, storeId);

        TicketVO vo = new TicketVO();
        vo.setTicketNo(ticketNoText);
        vo.setUserId(uid);
        vo.setStatus(TicketStatus.CALLED.getCode());
        vo.setStatusDesc(TicketStatus.CALLED.getDesc());
        return vo;
    }

    /**
     * 取消取号
     *
     * @param storeId      门店ID
     * @param templateId   队列模板ID
     * @param uid          用户ID，店员操作传空字符串
     * @param ticketNoText 号码文本
     */
    public void cancel(Long storeId, Long templateId, String uid, String ticketNoText) {
        QueueConfig config = getConfig(storeId, templateId);
        long ticketNo = QueueTakeService.parseTicketNo(config.getPrefix(), ticketNoText);
        List<?> result = redisQueueService.cancel(storeId, templateId, uid, ticketNo);
        if (toLong(result.get(0)) != 1L) {
            throw new BizException(ErrorCodeEnum.TICKET_STATUS_ILLEGAL, mapMessage(String.valueOf(result.get(2))));
        }
        String ticketUid = String.valueOf(result.get(1));
        sendEvent(QueueEventMessage.EVENT_CANCELLED, storeId, templateId, ticketUid, ticketNo, ticketNoText, null);
        redisQueueService.clearActiveTicketNo(ticketUid, storeId);
    }

    /**
     * 完成服务
     *
     * @param storeId      门店ID
     * @param templateId   队列模板ID
     * @param uid          用户ID，店员操作传空字符串
     * @param ticketNoText 号码文本
     */
    public void finish(Long storeId, Long templateId, String uid, String ticketNoText) {
        QueueConfig config = getConfig(storeId, templateId);
        long ticketNo = QueueTakeService.parseTicketNo(config.getPrefix(), ticketNoText);
        List<?> result = redisQueueService.finish(storeId, templateId, uid, ticketNo);
        if (toLong(result.get(0)) != 1L) {
            throw new BizException(ErrorCodeEnum.TICKET_STATUS_ILLEGAL, mapMessage(String.valueOf(result.get(2))));
        }
        String ticketUid = String.valueOf(result.get(1));
        sendEvent(QueueEventMessage.EVENT_FINISHED, storeId, templateId, ticketUid, ticketNo, ticketNoText, null);
        redisQueueService.clearActiveTicketNo(ticketUid, storeId);
    }

    /**
     * 过号
     *
     * @param storeId      门店ID
     * @param templateId   队列模板ID
     * @param ticketNoText 号码文本
     */
    public void miss(Long storeId, Long templateId, String ticketNoText) {
        QueueConfig config = getConfig(storeId, templateId);
        long ticketNo = QueueTakeService.parseTicketNo(config.getPrefix(), ticketNoText);
        List<?> result = redisQueueService.miss(storeId, templateId, ticketNo);
        if (toLong(result.get(0)) != 1L) {
            throw new BizException(ErrorCodeEnum.TICKET_STATUS_ILLEGAL, mapMessage(String.valueOf(result.get(2))));
        }
        String ticketUid = String.valueOf(result.get(1));
        sendEvent(QueueEventMessage.EVENT_MISSED, storeId, templateId, ticketUid, ticketNo, ticketNoText, null);
        redisQueueService.clearActiveTicketNo(ticketUid, storeId);
    }

    private void sendEvent(String eventType, Long storeId, Long templateId, String uid,
                           long ticketNo, String ticketNoText, Long counterId) {
        QueueEventMessage message = new QueueEventMessage();
        message.setMessageId(UUID.randomUUID().toString());
        message.setEventType(eventType);
        message.setStoreId(storeId);
        message.setTemplateId(templateId);
        message.setUid(uid);
        message.setTicketNo(ticketNo);
        message.setTicketNoText(ticketNoText);
        message.setCounterId(counterId);
        message.setEventTime(System.currentTimeMillis());
        producer.send(message);
    }

    private QueueConfig getConfig(Long storeId, Long templateId) {
        QueueConfig config = queueConfigCache.get(storeId, templateId);
        if (config == null) {
            throw new BizException(ErrorCodeEnum.PARAM_ERROR, "队列不存在");
        }
        return config;
    }

    private String mapMessage(String code) {
        return switch (code) {
            case "QUEUE_EMPTY" -> "当前队列没有等待号码";
            case "CURRENT_NOT_FINISHED" -> "请先完成或过号当前号码";
            case "TICKET_NOT_FOUND" -> "取号单不存在";
            case "FORBIDDEN" -> "无权操作该取号单";
            case "STATUS_ILLEGAL" -> "当前状态不允许该操作";
            default -> "操作失败";
        };
    }

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
