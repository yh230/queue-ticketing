package com.queue.ticketing.modules.notify.channel;

import com.queue.ticketing.modules.notify.entity.NotifyLog;

/**
 * 通知渠道接口
 *
 * @author yh
 * @date 2026-10-22
 */
public interface NotificationChannel {

    /**
     * 渠道编码
     *
     * @return 编码
     */
    String channelCode();

    /**
     * 发送通知
     *
     * @param log 通知日志
     * @return 是否成功
     */
    boolean send(NotifyLog log);
}
