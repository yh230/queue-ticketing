package com.queue.ticketing.modules.ticket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 叫号请求
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
public class CallTicketRequest {

    /**
     * 队列实例ID
     */
    @NotNull(message = "队列实例ID不能为空")
    private Long queueInstanceId;

    /**
     * 柜台ID
     */
    @NotNull(message = "柜台ID不能为空")
    private Long counterId;
}
