package com.queue.ticketing.modules.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.queue.ticketing.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工账号实体
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("staff")
public class Staff extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 所属门店ID
     */
    private Long storeId;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 登录密码（加密后）
     */
    private String password;

    /**
     * 角色：ADMIN/STORE_MANAGER/STAFF
     */
    private String role;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;
}
