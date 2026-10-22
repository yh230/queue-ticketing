package com.queue.ticketing.modules.queue.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 本地消息重试任务
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalMessageRetryJob {

    private final LocalMessageService localMessageService;
    private final QueueEventProducer producer;

    /**
     * 每 10 秒扫描一次待重试消息
     */
    @Scheduled(fixedDelay = 10 * 1000L)
    public void retry() {
        List<LocalMessage> messages = localMessageService.findRetryable();
        for (LocalMessage message : messages) {
            try {
                producer.resend(message.getTopic(), message.getMessageId(), message.getPayload());
                localMessageService.markSuccess(message.getId());
            } catch (Exception e) {
                log.error("本地消息重试失败，id={}", message.getId(), e);
                localMessageService.markFailed(message.getId());
            }
        }
    }
}
