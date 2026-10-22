package com.queue.ticketing.modules.queue.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 队列规则
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
public class QueueRule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 最大容量
     */
    private Integer capacity;

    /**
     * 过号后顺延几号重新排队，0表示直接作废
     */
    private Integer missedDeferCount = 0;

    /**
     * 允许的最大过号次数
     */
    private Integer maxMissedCount = 1;

    /**
     * 前方剩余人数提醒阈值
     */
    private Integer aheadNotifyThreshold = 3;

    /**
     * 是否允许用户主动取消
     */
    private Boolean allowCancel = true;

    /**
     * 号码过期时间（分钟），0表示不过期
     */
    private Integer expireMinutes = 0;
}
