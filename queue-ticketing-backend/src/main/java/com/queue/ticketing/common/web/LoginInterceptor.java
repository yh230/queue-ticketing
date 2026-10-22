package com.queue.ticketing.common.web;

import com.queue.ticketing.common.exception.ErrorCodeEnum;
import com.queue.ticketing.common.annotation.RequireAuth;
import com.queue.ticketing.common.util.JwtUtil;
import com.queue.ticketing.modules.auth.context.LoginContext;
import com.queue.ticketing.modules.auth.dto.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录认证拦截器
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireAuth classAuth = handlerMethod.getBeanType().getAnnotation(RequireAuth.class);
        RequireAuth methodAuth = handlerMethod.getMethodAnnotation(RequireAuth.class);
        if (classAuth == null && methodAuth == null) {
            return true;
        }

        String token = extractToken(request);
        if (token == null || token.isBlank()) {
            throw new com.queue.ticketing.common.exception.BizException(ErrorCodeEnum.UNAUTHORIZED);
        }

        LoginUser loginUser;
        try {
            loginUser = jwtUtil.parseToken(token);
        } catch (Exception e) {
            log.warn("Token 解析失败", e);
            throw new com.queue.ticketing.common.exception.BizException(ErrorCodeEnum.UNAUTHORIZED, "Token 无效或已过期");
        }

        RequireAuth effectiveAuth = methodAuth != null ? methodAuth : classAuth;
        if (!effectiveAuth.allowClient() && "CLIENT".equals(loginUser.getUserType())) {
            throw new com.queue.ticketing.common.exception.BizException(ErrorCodeEnum.FORBIDDEN, "该接口不允许 C 端用户访问");
        }

        LoginContext.set(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginContext.clear();
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
