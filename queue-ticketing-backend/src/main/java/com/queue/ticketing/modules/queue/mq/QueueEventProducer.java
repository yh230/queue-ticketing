package com.queue.ticketing.modules.queue.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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

    private static final int BUFFER_CAPACITY = 10000;
    private static final int SENDER_THREADS = 4;
    private static final long SEND_TIMEOUT_MILLIS = 500L;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final LocalMessageService localMessageService;

    private final BlockingQueue<QueueEventMessage> buffer = new LinkedBlockingQueue<>(BUFFER_CAPACITY);
    private final ExecutorService senderPool = Executors.newFixedThreadPool(SENDER_THREADS);

    @PostConstruct
    public void init() {
        for (int i = 0; i < SENDER_THREADS; i++) {
            senderPool.submit(this::sendLoop);
        }
    }

    /**
     * 发送排队事件：只入内存队列，不阻塞请求线程
     *
     * @param message 事件消息
     */
    public void send(QueueEventMessage message) {
        if (!buffer.offer(message)) {
            log.warn("排队事件缓冲区已满，降级写本地消息表，messageId={}", message.getMessageId());
            saveLocal(message);
        }
    }

    private void sendLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            QueueEventMessage message = null;
            try {
                message = buffer.take();
                String payload = objectMapper.writeValueAsString(message);
                kafkaTemplate.send(TOPIC, String.valueOf(message.getStoreId()), payload)
                        .get(SEND_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.error("发送排队事件失败，messageId={}", message == null ? null : message.getMessageId(), e);
                if (message != null) {
                    saveLocal(message);
                }
            }
        }
    }

    private void saveLocal(QueueEventMessage message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            localMessageService.savePending(TOPIC, message.getMessageId(), payload);
        } catch (Exception e) {
            log.error("保存本地消息失败，messageId={}", message.getMessageId(), e);
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
        kafkaTemplate.send(topic, key, payload).get(3, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy() {
        senderPool.shutdownNow();
    }
}
