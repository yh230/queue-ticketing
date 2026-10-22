package com.queue.ticketing.modules.notify.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.queue.ticketing.modules.notify.entity.Subscription;
import com.queue.ticketing.modules.notify.mapper.SubscriptionMapper;
import org.springframework.stereotype.Service;

/**
 * 通知订阅服务
 *
 * @author yh
 * @date 2026-10-22
 */
@Service
public class SubscriptionService extends ServiceImpl<SubscriptionMapper, Subscription> {

    /**
     * 订阅取号单通知
     *
     * @param ticketId   取号单ID
     * @param userId     用户ID
     * @param notifyType 通知类型
     * @param threshold  阈值
     */
    public void subscribe(Long ticketId, String userId, String notifyType, Integer threshold) {
        Subscription subscription = new Subscription();
        subscription.setTicketId(ticketId);
        subscription.setUserId(userId);
        subscription.setNotifyType(notifyType);
        subscription.setThreshold(threshold);
        getBaseMapper().insert(subscription);
    }
}
