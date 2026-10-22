package com.queue.ticketing.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "redisson")
public class RedissonConfig {

    /**
     * Redis 地址
     */
    private String address = "redis://127.0.0.1:6379";

    /**
     * 数据库索引
     */
    private int database = 0;

    /**
     * 超时时间（毫秒）
     */
    private int timeout = 3000;

    /**
     * 创建 RedissonClient
     *
     * @return RedissonClient
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(address)
                .setDatabase(database)
                .setTimeout(timeout)
                .setConnectionMinimumIdleSize(2)
                .setConnectionPoolSize(20);
        return Redisson.create(config);
    }
}
