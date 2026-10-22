package com.queue.ticketing.modules.ticket.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.queue.ticketing.modules.ticket.entity.Ticket;
import com.queue.ticketing.modules.ticket.enums.TicketStatus;
import com.queue.ticketing.modules.ticket.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 取号单过期清理任务
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketExpireJob {

    private final TicketMapper ticketMapper;

    /**
     * 每 5 分钟清理一次过期号码
     * 过期规则：取号超过 2 小时仍未叫到号（WAITING 状态）
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000L)
    public void expireTickets() {
        LocalDateTime expireTime = LocalDateTime.now().minusHours(2);
        int rows = ticketMapper.update(null,
                new LambdaUpdateWrapper<Ticket>()
                        .set(Ticket::getStatus, TicketStatus.EXPIRED.getCode())
                        .eq(Ticket::getStatus, TicketStatus.WAITING.getCode())
                        .lt(Ticket::getCreatedAt, expireTime)
        );
        if (rows > 0) {
            log.info("清理过期取号单 {} 条", rows);
        }
    }
}
