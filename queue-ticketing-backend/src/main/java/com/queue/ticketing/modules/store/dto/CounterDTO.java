package com.queue.ticketing.modules.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 柜台 DTO
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
public class CounterDTO {

    /**
     * 门店ID
     */
    @NotNull(message = "门店ID不能为空")
    private Long storeId;

    /**
     * 柜台名称
     */
    @NotBlank(message = "柜台名称不能为空")
    private String name;

    /**
     * 可服务队列模板ID列表
     */
    private List<Long> queueTemplateIds;
}
