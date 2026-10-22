package com.queue.ticketing.common.idempotent;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Optional;

/**
 * 默认幂等键解析器
 *
 * @author yh
 * @date 2026-10-22
 */
@Component
public class DefaultIdempotentKeyResolver implements IdempotentKeyResolver {

    private static final String HEADER_KEY = "Idempotency-Key";

    @Override
    public String resolve(ProceedingJoinPoint joinPoint, String keyExpr) {
        HttpServletRequest request = Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(attributes -> attributes instanceof ServletRequestAttributes)
                .map(attributes -> ((ServletRequestAttributes) attributes).getRequest())
                .orElse(null);

        if (request != null) {
            String headerValue = request.getHeader(HEADER_KEY);
            if (headerValue != null && !headerValue.isBlank()) {
                return headerValue;
            }
        }

        //  fallback：方法签名 + 第一个字符串参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();
        String firstStringArg = Arrays.stream(joinPoint.getArgs())
                .filter(arg -> arg instanceof String)
                .map(String::valueOf)
                .findFirst()
                .orElse("");
        return methodName + ":" + firstStringArg;
    }
}
