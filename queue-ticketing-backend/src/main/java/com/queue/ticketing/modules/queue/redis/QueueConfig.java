package com.queue.ticketing.modules.queue.redis;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 队列运行时配置
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
public class QueueConfig implements Serializable {

    @Serial
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
     * 队列名称
     */
    private String queueName;

    /**
     * 号码前缀
     */
    private String prefix;

    /**
     * 最大排队数
     */
    private Integer maxSize;

    /**
     * 数据过期时间（秒）
     */
    private Integer ttlSeconds = 86400;

    /**
     * 门店令牌桶速率（个/秒）
     */
    private Integer ratePerSecond = 200;

    /**
     * 门店令牌桶容量
     */
    private Integer rateCapacity = 400;

    /**
     * 是否开放
     */
    private Boolean open = true;
}
