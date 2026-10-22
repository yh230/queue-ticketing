package com.queue.ticketing.interfaces.advice;

import com.queue.ticketing.common.Result;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.UUID;

/**
 * 统一响应处理：注入 traceId
 *
 * @author yh
 * @date 2026-10-22
 */
@RestControllerAdvice(basePackages = "com.queue.ticketing.interfaces")
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getParameterType().isAssignableFrom(Result.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        String traceId = MDC.get("traceId");
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        response.getHeaders().add(TRACE_ID_HEADER, traceId);

        if (body instanceof Result) {
            ((Result<Object>) body).setTraceId(traceId);
        }
        return body;
    }
}
