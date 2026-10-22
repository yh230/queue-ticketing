package com.queue.ticketing.modules.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 门店 DTO
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
public class StoreDTO {

    /**
     * 商户ID
     */
    @NotNull(message = "商户ID不能为空")
    private Long merchantId;

    /**
     * 门店名称
     */
    @NotBlank(message = "门店名称不能为空")
    private String name;

    /**
     * 门店地址
     */
    private String address;

    /**
     * 营业时间
     */
    private String businessHours;
}
