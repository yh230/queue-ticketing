package com.queue.ticketing.modules.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.queue.ticketing.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 柜台与队列模板关联实体
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("counter_queue_template")
public class CounterQueueTemplate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 柜台ID
     */
    private Long counterId;

    /**
     * 队列模板ID
     */
    private Long queueTemplateId;
}
