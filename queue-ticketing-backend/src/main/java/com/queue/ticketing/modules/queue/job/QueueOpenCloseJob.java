package com.queue.ticketing.modules.queue.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.queue.ticketing.modules.queue.entity.QueueInstance;
import com.queue.ticketing.modules.queue.enums.QueueStatus;
import com.queue.ticketing.modules.queue.mapper.QueueInstanceMapper;
import com.queue.ticketing.modules.store.entity.Store;
import com.queue.ticketing.modules.store.mapper.StoreMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 队列开闭任务
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueueOpenCloseJob {

    private final StoreMapper storeMapper;
    private final QueueInstanceMapper queueInstanceMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 每分钟检查一次营业时间和队列状态
     */
    @Scheduled(fixedDelay = 60 * 1000L)
    public void checkQueueStatus() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Store> stores = storeMapper.selectList(
                new LambdaQueryWrapper<Store>().eq(Store::getStatus, 1)
        );

        for (Store store : stores) {
            boolean shouldOpen = isInBusinessHours(store.getBusinessHours(), now);
            QueueStatus targetStatus = shouldOpen ? QueueStatus.OPEN : QueueStatus.CLOSED;

            queueInstanceMapper.update(null,
                    new LambdaUpdateWrapper<QueueInstance>()
                            .set(QueueInstance::getStatus, targetStatus.getCode())
                            .eq(QueueInstance::getStoreId, store.getId())
                            .eq(QueueInstance::getQueueDate, today)
                            .ne(QueueInstance::getStatus, targetStatus.getCode())
            );
        }
    }

    private boolean isInBusinessHours(String businessHours, LocalTime now) {
        if (businessHours == null || businessHours.isBlank()) {
            return true;
        }
        String[] parts = businessHours.split("-");
        if (parts.length != 2) {
            return true;
        }
        try {
            LocalTime open = LocalTime.parse(parts[0].trim(), TIME_FORMATTER);
            LocalTime close = LocalTime.parse(parts[1].trim(), TIME_FORMATTER);
            return !now.isBefore(open) && !now.isAfter(close);
        } catch (Exception e) {
            log.warn("营业时间解析失败，businessHours={}", businessHours);
            return true;
        }
    }
}
