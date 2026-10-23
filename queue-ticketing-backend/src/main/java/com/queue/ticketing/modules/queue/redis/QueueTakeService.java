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
 * Redis 取号服务
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueueTakeService {

    private final QueueConfigCache queueConfigCache;
    private final RedisQueueService redisQueueService;
    private final RiskControlService riskControlService;
    private final QueueEventProducer producer;

    /**
     * 取号
     *
     * @param storeId    门店ID
     * @param templateId 队列模板ID
     * @param uid        用户ID
     * @param requestId  幂等请求ID
     * @param userPhone  用户手机号
     * @param source     取号来源
     * @return 取号结果
     */
    public TicketVO take(Long storeId, Long templateId, String uid, String requestId,
                         String userPhone, String source) {
        QueueConfig config = queueConfigCache.get(storeId, templateId);
        if (config == null) {
            throw new BizException(ErrorCodeEnum.PARAM_ERROR, "队列不存在");
        }
        if (!Boolean.TRUE.equals(config.getOpen())) {
            throw new BizException(ErrorCodeEnum.QUEUE_CLOSED);
        }

        // 门店维度令牌桶
        if (!redisQueueService.tryAcquireStoreToken(storeId, config)) {
            throw new BizException(ErrorCodeEnum.TOO_MANY_REQUESTS, "当前取号人数过多，请稍后再试");
        }

        // 用户频控
        if (!riskControlService.allowTake(uid)) {
            throw new BizException(ErrorCodeEnum.TOO_MANY_REQUESTS, "操作过于频繁，请稍后再试");
        }

        // 原子取号
        List<?> result;
        try {
            result = redisQueueService.take(storeId, templateId, uid, requestId, userPhone, source, config);
        } catch (Exception e) {
            log.error("Redis 取号异常，storeId={}，templateId={}", storeId, templateId, e);
            throw new BizException(ErrorCodeEnum.SYSTEM_ERROR, "系统繁忙，请稍后再试");
        }

        if (toLong(result.get(0)) != 1L) {
            throw new BizException(mapErrorCode(String.valueOf(result.get(3))));
        }

        long ticketNo = toLong(result.get(1));
        int ahead = (int) toLong(result.get(2));
        String ticketNoText = formatTicketNo(config.getPrefix(), ticketNo);

        redisQueueService.setActiveTicketNo(uid, storeId, ticketNo);
        redisQueueService.expireQueueKeys(storeId, templateId, ticketNo, config.getTtlSeconds());

        QueueEventMessage message = new QueueEventMessage();
        message.setMessageId(UUID.randomUUID().toString());
        message.setEventType(QueueEventMessage.EVENT_TAKE_SUCCESS);
        message.setStoreId(storeId);
        message.setTemplateId(templateId);
        message.setUid(uid);
        message.setTicketNo(ticketNo);
        message.setTicketNoText(ticketNoText);
        message.setUserPhone(userPhone);
        message.setSource(source);
        message.setEventTime(System.currentTimeMillis());
        producer.send(message);

        TicketVO vo = new TicketVO();
        vo.setQueueInstanceId(null);
        vo.setTicketNo(ticketNoText);
        vo.setUserId(uid);
        vo.setUserPhone(userPhone);
        vo.setStatus(TicketStatus.WAITING.getCode());
        vo.setStatusDesc(TicketStatus.WAITING.getDesc());
        vo.setAheadCount(ahead);
        vo.setEstimatedWaitMinutes(Math.max(ahead, 0) * 5);
        return vo;
    }

    /**
     * 格式化排队号
     *
     * @param prefix 前缀
     * @param seq    序号
     * @return 号码文本
     */
    public static String formatTicketNo(String prefix, long seq) {
        return prefix + String.format("%03d", seq);
    }

    /**
     * 解析号码序号
     *
     * @param prefix       前缀
     * @param ticketNoText 号码文本
     * @return 序号
     */
    public static long parseTicketNo(String prefix, String ticketNoText) {
        if (ticketNoText == null || !ticketNoText.startsWith(prefix)) {
            throw new BizException(ErrorCodeEnum.PARAM_ERROR, "排队号格式错误");
        }
        return Long.parseLong(ticketNoText.substring(prefix.length()));
    }

    private ErrorCodeEnum mapErrorCode(String code) {
        return switch (code) {
            case "QUEUE_FULL" -> ErrorCodeEnum.QUEUE_FULL;
            case "DUPLICATE" -> ErrorCodeEnum.TICKET_DUPLICATE;
            case "QUEUE_CLOSED" -> ErrorCodeEnum.QUEUE_CLOSED;
            default -> ErrorCodeEnum.BIZ_ERROR;
        };
    }

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
