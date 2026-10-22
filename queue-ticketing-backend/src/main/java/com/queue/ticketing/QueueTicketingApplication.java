package com.queue.ticketing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 排队叫号 SaaS 平台启动类
 *
 * @author yh
 * @date 2026-10-22
 */
@EnableAsync
@EnableScheduling
@ServletComponentScan
@MapperScan("com.queue.ticketing.modules.**.mapper")
@SpringBootApplication(scanBasePackages = "com.queue.ticketing")
public class QueueTicketingApplication {

    public static void main(String[] args) {
        SpringApplication.run(QueueTicketingApplication.class, args);
    }
}
