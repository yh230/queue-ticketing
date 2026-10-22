package com.queue.ticketing.modules.auth.context;

import com.queue.ticketing.modules.auth.dto.LoginUser;

/**
 * 登录上下文
 *
 * @author yh
 * @date 2026-10-22
 */
public final class LoginContext {

    private static final ThreadLocal<LoginUser> CURRENT_USER = new ThreadLocal<>();

    private LoginContext() {
    }

    /**
     * 设置当前登录用户
     *
     * @param loginUser 登录用户
     */
    public static void set(LoginUser loginUser) {
        CURRENT_USER.set(loginUser);
    }

    /**
     * 获取当前登录用户
     *
     * @return 登录用户
     */
    public static LoginUser get() {
        return CURRENT_USER.get();
    }

    /**
     * 清除当前登录用户
     */
    public static void clear() {
        CURRENT_USER.remove();
    }
}
