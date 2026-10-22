package com.queue.ticketing.interfaces.controller;

import com.queue.ticketing.common.Result;
import com.queue.ticketing.modules.auth.service.StaffService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 员工账号接口
 *
 * @author yh
 * @date 2026-10-22
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/staff")
public class StaffController {

    private final StaffService staffService;

    /**
     * 员工登录
     *
     * @param request 登录请求
     * @return JWT Token
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest request) {
        String token = staffService.login(request.getUsername(), request.getPassword());
        return Result.success(token);
    }

    /**
     * 登录请求
     */
    @Data
    public static class LoginRequest {
        @NotBlank(message = "账号不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
    }
}
