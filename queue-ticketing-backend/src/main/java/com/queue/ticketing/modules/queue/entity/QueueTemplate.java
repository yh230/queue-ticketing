package com.queue.ticketing.modules.queue.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.queue.ticketing.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 队列模板实体
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("queue_template")
public class QueueTemplate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 队列名称，如小桌/中桌/大桌
     */
    private String name;

    /**
     * 号码前缀，如 A/B/C
     */
    private String prefix;

    /**
     * 最大容量
     */
    private Integer capacity;

    /**
     * 队列规则JSON
     */
    private String rulesJson;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;
}
