package com.queue.ticketing.modules.queue.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 排队事件生产者
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueueEventProducer {

    /**
     * 排队事件主题
     */
    public static final String TOPIC = "queue-ticket-event";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final LocalMessageService localMessageService;

    /**
     * 发送排队事件，失败写入本地消息表
     *
     * @param message 事件消息
     */
    public void send(QueueEventMessage message) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("序列化排队事件失败，messageId={}", message.getMessageId(), e);
            return;
        }

        try {
            kafkaTemplate.send(TOPIC, String.valueOf(message.getStoreId()), payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("发送排队事件失败，messageId={}", message.getMessageId(), ex);
                            localMessageService.savePending(TOPIC, message.getMessageId(), payload);
                        }
                    });
        } catch (Exception e) {
            log.error("发送排队事件异常，messageId={}", message.getMessageId(), e);
            localMessageService.savePending(TOPIC, message.getMessageId(), payload);
        }
    }

    /**
     * 重新发送本地消息，同步等待结果
     *
     * @param topic   主题
     * @param key     消息key
     * @param payload 消息内容
     * @throws Exception 发送失败
     */
    public void resend(String topic, String key, String payload) throws Exception {
        kafkaTemplate.send(topic, key, payload).get(3, java.util.concurrent.TimeUnit.SECONDS);
    }
}
