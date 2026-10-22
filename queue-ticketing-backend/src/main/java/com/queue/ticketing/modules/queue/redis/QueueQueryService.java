package com.queue.ticketing.modules.queue.redis;

import cn.hutool.core.util.DesensitizedUtil;
import com.queue.ticketing.common.exception.BizException;
import com.queue.ticketing.common.exception.ErrorCodeEnum;
import com.queue.ticketing.modules.queue.vo.QueueStatusVO;
import com.queue.ticketing.modules.queue.vo.WaitingTicketVO;
import com.queue.ticketing.modules.ticket.enums.TicketStatus;
import com.queue.ticketing.modules.ticket.vo.TicketVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 排队查询服务：全部读 Redis
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueueQueryService {

    private final QueueConfigCache queueConfigCache;
    private final RedisQueueService redisQueueService;

    /**
     * 查询排队进度
     *
     * @param storeId      门店ID
     * @param templateId   队列模板ID
     * @param ticketNoText 号码文本
     * @return 进度
     */
    public TicketVO progress(Long storeId, Long templateId, String ticketNoText) {
        QueueConfig config = getConfig(storeId, templateId);
        long ticketNo = QueueTakeService.parseTicketNo(config.getPrefix(), ticketNoText);
        Map<String, String> data = redisQueueService.progress(storeId, templateId, ticketNo);
        if (data == null) {
            throw new BizException(ErrorCodeEnum.TICKET_NOT_FOUND);
        }
        return buildTicketVO(ticketNoText, data);
    }

    /**
     * 查询用户当前排队
     *
     * @param storeId 门店ID
     * @param uid     用户ID
     * @return 当前取号单，无则返回 null
     */
    public TicketVO my(Long storeId, String uid) {
        String ticketNo = redisQueueService.getActiveTicketNo(uid, storeId);
        if (ticketNo == null) {
            return null;
        }
        long seq = Long.parseLong(ticketNo);
        for (QueueConfig config : queueConfigCache.getByStore(storeId)) {
            Map<String, String> data = redisQueueService.progress(storeId, config.getTemplateId(), seq);
            if (data != null) {
                return buildTicketVO(QueueTakeService.formatTicketNo(config.getPrefix(), seq), data);
            }
        }
        return null;
    }

    /**
     * 门店队列总览
     *
     * @param storeId 门店ID
     * @return 队列状态列表
     */
    public List<QueueStatusVO> overview(Long storeId) {
        List<QueueStatusVO> result = new ArrayList<>();
        for (QueueConfig config : queueConfigCache.getByStore(storeId)) {
            Map<Object, Object> meta = redisQueueService.getMeta(storeId, config.getTemplateId());

            QueueStatusVO vo = new QueueStatusVO();
            vo.setStoreId(storeId);
            vo.setTemplateId(config.getTemplateId());
            vo.setQueueName(config.getQueueName());

            String status = meta.get("status") == null ? "OPEN" : String.valueOf(meta.get("status"));
            vo.setStatus(status);
            vo.setWaitCount(meta.get("waitingCount") == null ? 0
                    : Integer.parseInt(String.valueOf(meta.get("waitingCount"))));

            String calling = meta.get("callingTicketNo") == null ? null : String.valueOf(meta.get("callingTicketNo"));
            if (calling != null && !calling.isBlank()) {
                vo.setCurrentTicketNo(QueueTakeService.formatTicketNo(config.getPrefix(),
                        Long.parseLong(calling)));
            }

            List<String> members = redisQueueService.getWaitingMembers(storeId, config.getTemplateId(), 10);
            List<WaitingTicketVO> waitingTickets = new ArrayList<>();
            for (String member : members) {
                String[] parts = member.split(":");
                if (parts.length < 2) {
                    continue;
                }
                long ticketNo = Long.parseLong(parts[1]);
                WaitingTicketVO waiting = new WaitingTicketVO();
                waiting.setTicketNo(QueueTakeService.formatTicketNo(config.getPrefix(), ticketNo));

                Map<Object, Object> detail = redisQueueService.getTicketDetail(
                        storeId, config.getTemplateId(), ticketNo);
                String source = detail.get("source") == null ? "" : String.valueOf(detail.get("source"));
                waiting.setSource(source);
                waiting.setSourceDesc("STAFF".equals(source) ? "店员代取" : "小程序");
                String phone = detail.get("userPhone") == null ? null : String.valueOf(detail.get("userPhone"));
                waiting.setUserPhone(phone == null || phone.isBlank() ? null : DesensitizedUtil.mobilePhone(phone));
                waitingTickets.add(waiting);
            }
            vo.setWaitingTickets(waitingTickets);
            result.add(vo);
        }
        return result;
    }

    private TicketVO buildTicketVO(String ticketNoText, Map<String, String> data) {
        TicketVO vo = new TicketVO();
        vo.setTicketNo(ticketNoText);
        vo.setUserId(data.get("uid"));
        String phone = data.get("userPhone");
        vo.setUserPhone(phone == null || phone.isBlank() ? null : phone);
        vo.setStatus(data.get("status"));
        TicketStatus status = TicketStatus.of(data.get("status"));
        if (status != null) {
            vo.setStatusDesc(status.getDesc());
        }
        int ahead = Integer.parseInt(data.getOrDefault("aheadCount", "0"));
        vo.setAheadCount(ahead);
        vo.setEstimatedWaitMinutes(Math.max(ahead, 0) * 5);
        String createdAt = data.get("createdAt");
        if (createdAt != null) {
            vo.setCreatedAt(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(Long.parseLong(createdAt)), ZoneId.systemDefault()));
        }
        return vo;
    }

    private QueueConfig getConfig(Long storeId, Long templateId) {
        QueueConfig config = queueConfigCache.get(storeId, templateId);
        if (config == null) {
            throw new BizException(ErrorCodeEnum.PARAM_ERROR, "队列不存在");
        }
        return config;
    }
}
