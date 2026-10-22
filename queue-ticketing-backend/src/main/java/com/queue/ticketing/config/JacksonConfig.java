package com.queue.ticketing.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 配置
 *
 * @author yh
 * @date 2026-10-22
 */
@Configuration
public class JacksonConfig {

    /**
     * Long 统一序列化为字符串，避免前端 JavaScript 精度丢失
     *
     * @return Jackson 定制器
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> {
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
        };
    }
}
