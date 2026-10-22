package com.queue.ticketing.modules.ticket.enums;

import lombok.Getter;

/**
 * 取号单状态
 *
 * @author yh
 * @date 2026-10-22
 */
@Getter
public enum TicketStatus {

    /**
     * 等待中
     */
    WAITING("WAITING", "等待中"),

    /**
     * 已叫号
     */
    CALLED("CALLED", "已叫号"),

    /**
     * 已服务
     */
    SERVED("SERVED", "已服务"),

    /**
     * 已过号
     */
    MISSED("MISSED", "已过号"),

    /**
     * 已取消
     */
    CANCELLED("CANCELLED", "已取消"),

    /**
     * 已过期
     */
    EXPIRED("EXPIRED", "已过期");

    private final String code;
    private final String desc;

    TicketStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举
     *
     * @param code 状态码
     * @return 枚举
     */
    public static TicketStatus of(String code) {
        for (TicketStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
