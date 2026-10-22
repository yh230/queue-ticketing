package com.queue.ticketing.modules.notify.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.queue.ticketing.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知订阅实体
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("subscription")
public class Subscription extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 取号单ID
     */
    private Long ticketId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 通知类型：AHEAD/CALLED/MISSED
     */
    private String notifyType;

    /**
     * 前方剩余人数阈值
     */
    private Integer threshold;
}
