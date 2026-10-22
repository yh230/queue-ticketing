package com.queue.ticketing.modules.queue.mq;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 排队领域事件消息
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
public class QueueEventMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 取号成功
     */
    public static final String EVENT_TAKE_SUCCESS = "TAKE_SUCCESS";

    /**
     * 叫号
     */
    public static final String EVENT_CALLED = "TICKET_CALLED";

    /**
     * 取消
     */
    public static final String EVENT_CANCELLED = "TICKET_CANCELLED";

    /**
     * 完成服务
     */
    public static final String EVENT_FINISHED = "TICKET_FINISHED";

    /**
     * 过号
     */
    public static final String EVENT_MISSED = "TICKET_MISSED";

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 队列模板ID
     */
    private Long templateId;

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 排队号码数字
     */
    private Long ticketNo;

    /**
     * 排队号码文本，如 A001
     */
    private String ticketNoText;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 取号来源
     */
    private String source;

    /**
     * 柜台ID
     */
    private Long counterId;

    /**
     * 事件时间
     */
    private Long eventTime;
}
