package com.queue.ticketing.modules.ticket.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.queue.ticketing.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 取号单实体
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ticket")
public class Ticket extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 门店ID（冗余）
     */
    private Long storeId;

    /**
     * 队列实例ID
     */
    private Long queueInstanceId;

    /**
     * 排队号码，如 A001
     */
    private String ticketNo;

    /**
     * C端用户ID（微信openid）
     */
    private String userId;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 状态：WAITING/CALLED/SERVED/MISSED/CANCELLED/EXPIRED
     */
    private String status;

    /**
     * 来源：MINI_PROGRAM/STAFF
     */
    private String source;

    /**
     * 服务柜台ID
     */
    private Long counterId;

    /**
     * 叫号时间
     */
    private LocalDateTime calledAt;

    /**
     * 服务完成时间
     */
    private LocalDateTime servedAt;

    /**
     * 过号次数
     */
    private Integer missedCount;

    /**
     * 幂等键
     */
    private String idempotencyKey;

    /**
     * MQ 消息ID，用于消费幂等
     */
    private String messageId;
}
