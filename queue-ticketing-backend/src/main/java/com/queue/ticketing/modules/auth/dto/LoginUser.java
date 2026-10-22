package com.queue.ticketing.modules.auth.dto;

import lombok.Data;

/**
 * 登录用户信息
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
public class LoginUser {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 角色
     */
    private String role;

    /**
     * 用户类型：STAFF/CLIENT
     */
    private String userType;
}
