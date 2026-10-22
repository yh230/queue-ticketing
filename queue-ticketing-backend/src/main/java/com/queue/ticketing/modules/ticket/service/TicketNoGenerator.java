package com.queue.ticketing.modules.ticket.service;

import com.queue.ticketing.common.BizException;
import com.queue.ticketing.common.ErrorCodeEnum;
import com.queue.ticketing.common.QueueConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 排队号码生成器
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketNoGenerator {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 生成下一个排队号码
     *
     * @param queueInstanceId 队列实例ID
     * @param prefix          号码前缀
     * @return 排队号码
     */
    public String nextTicketNo(Long queueInstanceId, String prefix) {
        String seqKey = buildSeqKey(queueInstanceId);
        Long seq = redisTemplate.opsForValue().increment(seqKey);
        if (seq == null) {
            log.error("Redis 号码生成失败，queueInstanceId={}", queueInstanceId);
            throw new BizException(ErrorCodeEnum.TICKET_NO_GENERATE_FAILED);
        }
        String ticketNo = prefix + String.format("%0" + QueueConstants.TICKET_NO_LENGTH + "d", seq);
        log.debug("生成号码，queueInstanceId={}，ticketNo={}", queueInstanceId, ticketNo);
        return ticketNo;
    }

    /**
     * 重置号码序列
     *
     * @param queueInstanceId 队列实例ID
     */
    public void resetSeq(Long queueInstanceId) {
        String seqKey = buildSeqKey(queueInstanceId);
        redisTemplate.delete(seqKey);
        log.info("重置号码序列，queueInstanceId={}", queueInstanceId);
    }

    private String buildSeqKey(Long queueInstanceId) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        return QueueConstants.QUEUE_SEQ_KEY_PREFIX + queueInstanceId + ":" + today;
    }
}
