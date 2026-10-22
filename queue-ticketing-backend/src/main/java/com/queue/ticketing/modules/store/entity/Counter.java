package com.queue.ticketing.modules.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.queue.ticketing.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 柜台/窗口实体
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("counter")
public class Counter extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 柜台名称
     */
    private String name;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;
}
