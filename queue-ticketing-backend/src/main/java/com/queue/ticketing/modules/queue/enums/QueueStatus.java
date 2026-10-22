package com.queue.ticketing.modules.queue.enums;

import lombok.Getter;

/**
 * 队列实例状态
 *
 * @author yh
 * @date 2026-10-22
 */
@Getter
public enum QueueStatus {

    /**
     * 开放取号
     */
    OPEN("OPEN", "开放取号"),

    /**
     * 暂停取号
     */
    PAUSED("PAUSED", "暂停取号"),

    /**
     * 已关闭
     */
    CLOSED("CLOSED", "已关闭");

    private final String code;
    private final String desc;

    QueueStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
