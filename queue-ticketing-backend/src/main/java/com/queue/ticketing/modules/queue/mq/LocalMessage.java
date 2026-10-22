package com.queue.ticketing.modules.queue.mq;

import com.baomidou.mybatisplus.annotation.TableName;
import com.queue.ticketing.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 本地消息表实体
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("local_message")
public class LocalMessage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 主题
     */
    private String topic;

    /**
     * 消息内容
     */
    private String payload;

    /**
     * 状态：0-待发送 1-已发送 2-失败
     */
    private Integer status;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 下次重试时间
     */
    private LocalDateTime nextRetryAt;
}
