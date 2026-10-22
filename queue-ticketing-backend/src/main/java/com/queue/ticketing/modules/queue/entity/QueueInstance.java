package com.queue.ticketing.modules.queue.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.queue.ticketing.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 队列实例实体
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("queue_instance")
public class QueueInstance extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 队列模板ID
     */
    private Long templateId;

    /**
     * 队列日期
     */
    private LocalDate queueDate;

    /**
     * 状态：OPEN/PAUSED/CLOSED
     */
    private String status;

    /**
     * 当前叫号 ticket ID
     */
    private Long currentTicketId;

    /**
     * 当前等待人数
     */
    private Integer waitCount;
}
