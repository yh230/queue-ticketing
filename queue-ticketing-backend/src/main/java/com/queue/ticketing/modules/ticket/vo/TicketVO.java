package com.queue.ticketing.modules.ticket.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 取号单 VO
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
public class TicketVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 取号单ID
     */
    private Long id;

    /**
     * 队列实例ID
     */
    private Long queueInstanceId;

    /**
     * 排队号码
     */
    private String ticketNo;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 状态
     */
    private String status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 来源
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
     * 前方等待人数
     */
    private Integer aheadCount;

    /**
     * 预计等待时间（分钟）
     */
    private Integer estimatedWaitMinutes;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
