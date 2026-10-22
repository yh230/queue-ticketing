package com.queue.ticketing.modules.queue.redis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.queue.ticketing.modules.queue.entity.QueueTemplate;
import com.queue.ticketing.modules.queue.mapper.QueueTemplateMapper;
import com.queue.ticketing.modules.store.entity.Store;
import com.queue.ticketing.modules.store.mapper.StoreMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 队列运行时配置本地缓存
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueueConfigCache {

    private final StoreMapper storeMapper;
    private final QueueTemplateMapper queueTemplateMapper;

    private final Map<String, QueueConfig> cache = new ConcurrentHashMap<>();

    /**
     * 获取队列配置
     *
     * @param storeId    门店ID
     * @param templateId 队列模板ID
     * @return 配置，不存在返回 null
     */
    public QueueConfig get(Long storeId, Long templateId) {
        return cache.get(buildKey(storeId, templateId));
    }

    /**
     * 获取门店下所有队列配置
     *
     * @param storeId 门店ID
     * @return 配置列表
     */
    public List<QueueConfig> getByStore(Long storeId) {
        return cache.values().stream()
                .filter(config -> config.getStoreId().equals(storeId))
                .sorted(java.util.Comparator.comparing(QueueConfig::getTemplateId))
                .toList();
    }

    /**
     * 启动时加载队列配置
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        refresh();
    }

    /**
     * 每分钟刷新一次本地配置
     */
    @Scheduled(fixedDelay = 60 * 1000L)
    public void refresh() {
        try {
            List<Store> stores = storeMapper.selectList(
                    new LambdaQueryWrapper<Store>().eq(Store::getStatus, 1));
            List<QueueTemplate> templates = queueTemplateMapper.selectList(
                    new LambdaQueryWrapper<QueueTemplate>().eq(QueueTemplate::getStatus, 1));

            Map<String, QueueConfig> next = new ConcurrentHashMap<>();
            for (Store store : stores) {
                for (QueueTemplate template : templates) {
                    if (!store.getMerchantId().equals(template.getMerchantId())) {
                        continue;
                    }
                    QueueConfig config = new QueueConfig();
                    config.setStoreId(store.getId());
                    config.setTemplateId(template.getId());
                    config.setQueueName(template.getName());
                    config.setPrefix(template.getPrefix());
                    config.setMaxSize(template.getCapacity());
                    config.setTtlSeconds(86400);
                    config.setRatePerSecond(200);
                    config.setRateCapacity(400);
                    config.setOpen(true);
                    next.put(buildKey(store.getId(), template.getId()), config);
                }
            }
            cache.clear();
            cache.putAll(next);
            log.debug("队列配置缓存刷新完成，size={}", cache.size());
        } catch (Exception e) {
            log.error("队列配置缓存刷新失败", e);
        }
    }

    private String buildKey(Long storeId, Long templateId) {
        return storeId + ":" + templateId;
    }
}
