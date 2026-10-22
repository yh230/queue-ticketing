package com.queue.ticketing.modules.queue.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息服务
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Service
public class LocalMessageService extends ServiceImpl<LocalMessageMapper, LocalMessage> {

    /**
     * 保存待发送消息
     *
     * @param topic     主题
     * @param messageId 消息ID
     * @param payload   消息内容
     */
    public void savePending(String topic, String messageId, String payload) {
        LocalMessage message = new LocalMessage();
        message.setMessageId(messageId);
        message.setTopic(topic);
        message.setPayload(payload);
        message.setStatus(0);
        message.setRetryCount(0);
        message.setNextRetryAt(LocalDateTime.now());
        getBaseMapper().insert(message);
    }

    /**
     * 标记发送成功
     *
     * @param id 本地消息ID
     */
    public void markSuccess(Long id) {
        LocalMessage message = getById(id);
        if (message == null) {
            return;
        }
        message.setStatus(1);
        updateById(message);
    }

    /**
     * 标记发送失败并计算下次重试时间
     *
     * @param id 本地消息ID
     */
    public void markFailed(Long id) {
        LocalMessage message = getById(id);
        if (message == null) {
            return;
        }
        int retry = message.getRetryCount() == null ? 0 : message.getRetryCount();
        message.setRetryCount(retry + 1);
        message.setStatus(retry + 1 >= 10 ? 2 : 0);
        long delaySeconds = Math.min(300, 5L * (retry + 1));
        message.setNextRetryAt(LocalDateTime.now().plusSeconds(delaySeconds));
        updateById(message);
        log.warn("本地消息发送失败，id={}，retry={}", id, retry + 1);
    }

    /**
     * 查询待重试消息
     *
     * @return 消息列表
     */
    public List<LocalMessage> findRetryable() {
        return getBaseMapper().selectList(new LambdaQueryWrapper<LocalMessage>()
                .eq(LocalMessage::getStatus, 0)
                .le(LocalMessage::getNextRetryAt, LocalDateTime.now())
                .orderByAsc(LocalMessage::getId)
                .last("LIMIT 100"));
    }
}
