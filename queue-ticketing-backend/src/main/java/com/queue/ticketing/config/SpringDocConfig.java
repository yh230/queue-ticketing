package com.queue.ticketing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 配置
 *
 * @author yh
 * @date 2026-10-22
 */
@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Queue Ticketing API")
                        .description("排队叫号 SaaS 平台接口文档")
                        .version("1.0.0"));
    }
}
