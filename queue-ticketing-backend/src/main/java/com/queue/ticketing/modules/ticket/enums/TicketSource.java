package com.queue.ticketing.modules.ticket.enums;

import lombok.Getter;

/**
 * 取号来源
 *
 * @author yh
 * @date 2026-10-22
 */
@Getter
public enum TicketSource {

    /**
     * 微信小程序
     */
    MINI_PROGRAM("MINI_PROGRAM", "微信小程序"),

    /**
     * 店员代取
     */
    STAFF("STAFF", "店员代取");

    private final String code;
    private final String desc;

    TicketSource(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
