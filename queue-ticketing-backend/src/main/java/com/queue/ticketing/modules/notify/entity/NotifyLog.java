package com.queue.ticketing.modules.notify.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.queue.ticketing.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知日志实体
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notify_log")
public class NotifyLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 取号单ID
     */
    private Long ticketId;

    /**
     * 通知渠道：WECHAT_MINI/SMS
     */
    private String channel;

    /**
     * 模板编码
     */
    private String templateCode;

    /**
     * 状态：0-待发送 1-成功 2-失败
     */
    private Integer status;

    /**
     * 失败原因
     */
    private String errorMsg;

    /**
     * 发送时间
     */
    private LocalDateTime sentAt;
}
