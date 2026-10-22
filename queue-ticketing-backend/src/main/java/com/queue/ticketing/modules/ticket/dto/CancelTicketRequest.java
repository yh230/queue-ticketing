package com.queue.ticketing.modules.ticket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 取消取号请求
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
public class CancelTicketRequest {

    /**
     * 取号单ID
     */
    @NotNull(message = "取号单ID不能为空")
    private Long ticketId;

    /**
     * 用户ID，由登录上下文注入
     */
    private String userId;
}
