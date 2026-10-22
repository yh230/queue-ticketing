package com.queue.ticketing.modules.notify.channel;

import com.queue.ticketing.modules.notify.entity.NotifyLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 短信通知渠道（预留）
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Component
public class SmsChannel implements NotificationChannel {

    @Override
    public String channelCode() {
        return "SMS";
    }

    @Override
    public boolean send(NotifyLog notifyLog) {
        // 真实场景：调用阿里云/腾讯云短信接口
        log.info("发送短信通知，ticketId={}，templateCode={}",
                notifyLog.getTicketId(), notifyLog.getTemplateCode());
        return true;
    }
}
