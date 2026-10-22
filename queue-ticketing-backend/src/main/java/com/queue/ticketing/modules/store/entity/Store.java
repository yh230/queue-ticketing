package com.queue.ticketing.modules.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.queue.ticketing.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门店实体
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("store")
public class Store extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 门店名称
     */
    private String name;

    /**
     * 门店地址
     */
    private String address;

    /**
     * 营业时间
     */
    private String businessHours;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;
}
