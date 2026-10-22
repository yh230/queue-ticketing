package com.queue.ticketing.modules.ticket.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.queue.ticketing.common.constant.QueueConstants;
import com.queue.ticketing.modules.queue.mapper.QueueInstanceMapper;
import com.queue.ticketing.modules.ticket.entity.Ticket;
import com.queue.ticketing.modules.ticket.enums.TicketStatus;
import com.queue.ticketing.modules.ticket.mapper.TicketMapper;
import com.queue.ticketing.modules.ticket.service.QueueCapacityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final QueueInstanceMapper queueInstanceMapper;
    private final QueueCapacityService queueCapacityService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 每 5 分钟清理一次过期号码
     * 过期规则：取号超过 2 小时仍未叫到号（WAITING 状态）
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000L)
    public void expireTickets() {
        LocalDateTime expireTime = LocalDateTime.now().minusHours(2);
        List<Ticket> expiredTickets = ticketMapper.selectList(
                new LambdaQueryWrapper<Ticket>()
                        .eq(Ticket::getStatus, TicketStatus.WAITING.getCode())
                        .lt(Ticket::getCreatedAt, expireTime)
        );
        if (expiredTickets.isEmpty()) {
            return;
        }

        Map<Long, Long> countByInstance = expiredTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getQueueInstanceId, Collectors.counting()));

        for (Ticket ticket : expiredTickets) {
            ticket.setStatus(TicketStatus.EXPIRED.getCode());
            ticketMapper.updateById(ticket);
            if (ticket.getUserId() != null) {
                redisTemplate.delete(QueueConstants.USER_ACTIVE_KEY_PREFIX + ticket.getUserId());
            }
        }

        countByInstance.forEach((instanceId, count) -> {
            int releaseCount = count.intValue();
            queueCapacityService.release(instanceId, releaseCount);
            queueInstanceMapper.updateCurrentTicket(instanceId, null, -releaseCount);
        });

        log.info("过期取号单处理完成，count={}", expiredTickets.size());
    }
}
