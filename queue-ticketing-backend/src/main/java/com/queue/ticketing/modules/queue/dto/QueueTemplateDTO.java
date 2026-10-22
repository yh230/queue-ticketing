package com.queue.ticketing.modules.queue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 队列模板 DTO
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
public class QueueTemplateDTO {

    /**
     * 商户ID
     */
    @NotNull(message = "商户ID不能为空")
    private Long merchantId;

    /**
     * 队列名称
     */
    @NotBlank(message = "队列名称不能为空")
    private String name;

    /**
     * 号码前缀
     */
    @NotBlank(message = "号码前缀不能为空")
    private String prefix;

    /**
     * 最大容量
     */
    @NotNull(message = "最大容量不能为空")
    private Integer capacity;

    /**
     * 队列规则JSON
     */
    private String rulesJson;
}
