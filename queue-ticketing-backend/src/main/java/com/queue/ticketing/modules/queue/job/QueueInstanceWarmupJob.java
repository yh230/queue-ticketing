package com.queue.ticketing.modules.queue.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.queue.ticketing.modules.queue.entity.QueueInstance;
import com.queue.ticketing.modules.queue.entity.QueueTemplate;
import com.queue.ticketing.modules.queue.enums.QueueStatus;
import com.queue.ticketing.modules.queue.mapper.QueueInstanceMapper;
import com.queue.ticketing.modules.queue.mapper.QueueTemplateMapper;
import com.queue.ticketing.modules.store.entity.Counter;
import com.queue.ticketing.modules.store.mapper.CounterMapper;
import com.queue.ticketing.modules.store.mapper.CounterQueueTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 队列实例预热任务
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueueInstanceWarmupJob {

    private final QueueTemplateMapper queueTemplateMapper;
    private final CounterMapper counterMapper;
    private final CounterQueueTemplateMapper counterQueueTemplateMapper;
    private final QueueInstanceMapper queueInstanceMapper;

    /**
     * 每天 23:30 预热次日队列实例
     */
    @Scheduled(cron = "0 30 23 * * ?")
    public void warmupNextDayInstances() {
        LocalDate nextDate = LocalDate.now().plusDays(1);
        log.info("开始预热次日队列实例，date={}", nextDate);

        int pageSize = 100;
        int current = 1;
        while (true) {
            Page<QueueTemplate> page = new Page<>(current, pageSize);
            Page<QueueTemplate> result = queueTemplateMapper.selectPage(page,
                    new LambdaQueryWrapper<QueueTemplate>().eq(QueueTemplate::getStatus, 1));
            List<QueueTemplate> templates = result.getRecords();
            if (templates.isEmpty()) {
                break;
            }

            for (QueueTemplate template : templates) {
                List<Long> counterIds = counterQueueTemplateMapper.selectCounterIdsByTemplateId(template.getId());
                if (counterIds.isEmpty()) {
                    continue;
                }
                List<Counter> counters = counterMapper.selectBatchIds(counterIds);
                for (Counter counter : counters) {
                    if (counter.getStatus() == 1) {
                        createInstanceIfNotExists(counter.getStoreId(), template.getId(), nextDate);
                    }
                }
            }

            if (!result.hasNext()) {
                break;
            }
            current++;
        }

        log.info("次日队列实例预热完成");
    }

    private void createInstanceIfNotExists(Long storeId, Long templateId, LocalDate queueDate) {
        QueueInstance exists = queueInstanceMapper.selectByStoreAndDate(storeId, templateId, queueDate);
        if (exists != null) {
            return;
        }
        QueueInstance instance = new QueueInstance();
        instance.setStoreId(storeId);
        instance.setTemplateId(templateId);
        instance.setQueueDate(queueDate);
        instance.setStatus(QueueStatus.OPEN.getCode());
        instance.setWaitCount(0);
        queueInstanceMapper.insert(instance);
    }
}
