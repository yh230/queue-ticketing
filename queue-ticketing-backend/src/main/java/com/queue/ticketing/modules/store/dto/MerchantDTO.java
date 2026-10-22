package com.queue.ticketing.modules.store.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商户 DTO
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
public class MerchantDTO {

    /**
     * 商户名称
     */
    @NotBlank(message = "商户名称不能为空")
    private String name;

    /**
     * 行业类型
     */
    @NotBlank(message = "行业类型不能为空")
    private String industryType;
}
