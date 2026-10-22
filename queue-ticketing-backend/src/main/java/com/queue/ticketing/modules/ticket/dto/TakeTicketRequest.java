package com.queue.ticketing.modules.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 取号请求
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
public class TakeTicketRequest {

    /**
     * 队列模板ID
     */
    @NotNull(message = "队列模板ID不能为空")
    private Long templateId;

    /**
     * 门店ID
     */
    @NotNull(message = "门店ID不能为空")
    private Long storeId;

    /**
     * 用户ID（微信openid），由登录上下文注入
     */
    private String userId;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 来源：MINI_PROGRAM/STAFF
     */
    @NotBlank(message = "来源不能为空")
    private String source;

    /**
     * 取号日期，为空默认当天
     */
    private LocalDate queueDate;

    /**
     * 幂等键
     */
    @NotBlank(message = "幂等键不能为空")
    private String idempotencyKey;
}
