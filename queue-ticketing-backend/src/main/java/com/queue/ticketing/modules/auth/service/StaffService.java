package com.queue.ticketing.modules.auth.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.queue.ticketing.common.BizException;
import com.queue.ticketing.common.ErrorCodeEnum;
import com.queue.ticketing.common.util.JwtUtil;
import com.queue.ticketing.modules.auth.dto.LoginUser;
import com.queue.ticketing.modules.auth.entity.Staff;
import com.queue.ticketing.modules.auth.mapper.StaffMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 员工账号服务
 *
 * @author yh
 * @date 2026-10-22
 */
@Service
@RequiredArgsConstructor
public class StaffService extends ServiceImpl<StaffMapper, Staff> {

    private final JwtUtil jwtUtil;

    /**
     * 员工登录
     *
     * @param username 账号
     * @param password 密码
     * @return JWT Token
     */
    public String login(String username, String password) {
        Staff staff = getBaseMapper().selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Staff>()
                        .eq(Staff::getUsername, username)
                        .eq(Staff::getStatus, 1)
        );
        if (staff == null) {
            throw new BizException(ErrorCodeEnum.UNAUTHORIZED, "账号或密码错误");
        }

        String encryptedPassword = DigestUtil.md5Hex(password);
        if (!encryptedPassword.equals(staff.getPassword())) {
            throw new BizException(ErrorCodeEnum.UNAUTHORIZED, "账号或密码错误");
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(String.valueOf(staff.getId()));
        loginUser.setStoreId(staff.getStoreId());
        loginUser.setRole(staff.getRole());
        loginUser.setUserType("STAFF");
        return jwtUtil.generateToken(loginUser);
    }
}
