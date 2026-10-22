package com.queue.ticketing.common.util;

import com.queue.ticketing.modules.auth.dto.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Component
public class JwtUtil {

    private static final long TOKEN_EXPIRE_MILLIS = 24 * 60 * 60 * 1000L;

    @Value("${jwt.secret:queue-ticketing-default-secret-key-123456789012345678901234567890}")
    private String secret;

    private SecretKey key;

    private SecretKey getKey() {
        if (key == null) {
            key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
        return key;
    }

    /**
     * 生成 Token
     *
     * @param loginUser 登录用户
     * @return Token
     */
    public String generateToken(LoginUser loginUser) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + TOKEN_EXPIRE_MILLIS);
        return Jwts.builder()
                .subject(String.valueOf(loginUser.getUserId()))
                .claim("storeId", loginUser.getStoreId())
                .claim("role", loginUser.getRole())
                .claim("userType", loginUser.getUserType())
                .issuedAt(now)
                .expiration(expire)
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析 Token
     *
     * @param token Token
     * @return 登录用户
     */
    public LoginUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(claims.getSubject());
        loginUser.setStoreId(claims.get("storeId", Long.class));
        loginUser.setRole(claims.get("role", String.class));
        loginUser.setUserType(claims.get("userType", String.class));
        return loginUser;
    }
}
