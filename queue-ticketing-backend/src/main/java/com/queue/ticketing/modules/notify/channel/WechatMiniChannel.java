package com.queue.ticketing.modules.notify.channel;

import com.queue.ticketing.modules.notify.entity.NotifyLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 微信小程序订阅消息通知渠道
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Component
public class WechatMiniChannel implements NotificationChannel {

    @Override
    public String channelCode() {
        return "WECHAT_MINI";
    }

    @Override
    public boolean send(NotifyLog log) {
        // 真实场景：调用微信小程序订阅消息发送接口
        // 此处记录日志模拟发送
        log.info("发送微信小程序订阅消息，ticketId={}，templateCode={}", log.getTicketId(), log.getTemplateCode());
        return true;
    }
}
