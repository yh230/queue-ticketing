package com.queue.ticketing.modules.queue.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

/**
 * Redis Lua 脚本配置
 *
 * @author yh
 * @date 2026-10-22
 */
@Configuration
public class RedisLuaConfig {

    /**
     * 取号脚本
     *
     * @return 脚本
     */
    @Bean
    public DefaultRedisScript<List> takeTicketScript() {
        return load("lua/take_ticket.lua");
    }

    /**
     * 叫号脚本
     *
     * @return 脚本
     */
    @Bean
    public DefaultRedisScript<List> callTicketScript() {
        return load("lua/call_ticket.lua");
    }

    /**
     * 取消取号脚本
     *
     * @return 脚本
     */
    @Bean
    public DefaultRedisScript<List> cancelTicketScript() {
        return load("lua/cancel_ticket.lua");
    }

    /**
     * 完成服务脚本
     *
     * @return 脚本
     */
    @Bean
    public DefaultRedisScript<List> finishTicketScript() {
        return load("lua/finish_ticket.lua");
    }

    /**
     * 过号脚本
     *
     * @return 脚本
     */
    @Bean
    public DefaultRedisScript<List> missTicketScript() {
        return load("lua/miss_ticket.lua");
    }

    /**
     * 门店限流脚本
     *
     * @return 脚本
     */
    @Bean
    public DefaultRedisScript<Long> storeRateLimitScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/store_rate_limit.lua"));
        script.setResultType(Long.class);
        return script;
    }

    @SuppressWarnings("unchecked")
    private DefaultRedisScript<List> load(String path) {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(path));
        script.setResultType(List.class);
        return script;
    }
}
