package com.queue.ticketing.interfaces.controller;

import com.queue.ticketing.common.Result;
import com.queue.ticketing.common.util.JwtUtil;
import com.queue.ticketing.modules.auth.dto.LoginUser;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端用户登录接口
 *
 * @author yh
 * @date 2026-10-22
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/client")
public class ClientLoginController {

    private final JwtUtil jwtUtil;

    /**
     * C 端登录（ mock：手机号直接登录，真实场景应校验短信验证码）
     *
     * @param request 登录请求
     * @return JWT Token
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody ClientLoginRequest request) {
        LoginUser loginUser = new LoginUser();
        // 使用手机号作为用户ID（真实场景应使用微信 openid）
        loginUser.setUserId(request.getPhone());
        loginUser.setUserType("CLIENT");
        loginUser.setRole("CLIENT");
        String token = jwtUtil.generateToken(loginUser);
        return Result.success(token);
    }

    /**
     * C 端登录请求
     */
    @Data
    public static class ClientLoginRequest {
        @NotBlank(message = "手机号不能为空")
        private String phone;
    }
}
