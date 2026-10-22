package com.queue.ticketing.modules.queue.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 等待中的取号单
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
public class WaitingTicketVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 取号单ID
     */
    private Long ticketId;

    /**
     * 排队号码
     */
    private String ticketNo;

    /**
     * 状态
     */
    private String status;

    /**
     * 取号来源：MINI_PROGRAM/STAFF
     */
    private String source;

    /**
     * 来源描述
     */
    private String sourceDesc;

    /**
     * 脱敏手机号
     */
    private String userPhone;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
