package com.queue.ticketing.modules.ticket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 过号请求
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
public class MissTicketRequest {

    /**
     * 取号单ID
     */
    @NotNull(message = "取号单ID不能为空")
    private Long ticketId;
}
