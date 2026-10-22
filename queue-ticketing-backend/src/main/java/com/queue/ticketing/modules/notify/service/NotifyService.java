package com.queue.ticketing.modules.notify.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.queue.ticketing.modules.notify.channel.NotificationChannel;
import com.queue.ticketing.modules.notify.entity.NotifyLog;
import com.queue.ticketing.modules.notify.mapper.NotifyLogMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通知服务
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyService extends ServiceImpl<NotifyLogMapper, NotifyLog> {

    private final List<NotificationChannel> channels;
    private Map<String, NotificationChannel> channelMap;

    @PostConstruct
    public void init() {
        channelMap = channels.stream()
                .collect(Collectors.toMap(NotificationChannel::channelCode, Function.identity(), (a, b) -> a));
    }

    /**
     * 发送通知
     *
     * @param ticketId     取号单ID
     * @param channelCode  渠道编码
     * @param templateCode 模板编码
     */
    public void sendNotify(Long ticketId, String channelCode, String templateCode) {
        NotificationChannel channel = channelMap.get(channelCode);
        if (channel == null) {
            log.warn("通知渠道不存在，channelCode={}", channelCode);
            return;
        }

        NotifyLog logEntity = new NotifyLog();
        logEntity.setTicketId(ticketId);
        logEntity.setChannel(channelCode);
        logEntity.setTemplateCode(templateCode);
        logEntity.setStatus(0);
        getBaseMapper().insert(logEntity);

        boolean success = false;
        String lastError = null;
        int maxRetry = 3;
        for (int i = 0; i < maxRetry; i++) {
            try {
                success = channel.send(logEntity);
                if (success) {
                    break;
                }
                lastError = "第" + (i + 1) + "次发送返回失败";
            } catch (Exception e) {
                log.error("通知发送异常，ticketId={}，channel={}，retry={}", ticketId, channelCode, i, e);
                lastError = e.getMessage();
            }
        }

        logEntity.setStatus(success ? 1 : 2);
        logEntity.setErrorMsg(lastError);
        logEntity.setSentAt(LocalDateTime.now());
        updateById(logEntity);
    }
}
