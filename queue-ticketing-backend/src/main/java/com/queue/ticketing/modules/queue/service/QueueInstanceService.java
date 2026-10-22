package com.queue.ticketing.modules.queue.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.queue.ticketing.common.constant.QueueConstants;
import com.queue.ticketing.common.util.DistributedLockUtil;
import com.queue.ticketing.modules.queue.entity.QueueInstance;
import com.queue.ticketing.modules.queue.enums.QueueStatus;
import com.queue.ticketing.modules.queue.mapper.QueueInstanceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 队列实例服务
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueueInstanceService extends ServiceImpl<QueueInstanceMapper, QueueInstance> {

    private final DistributedLockUtil distributedLockUtil;

    /**
     * 获取或创建队列实例
     *
     * @param storeId    门店ID
     * @param templateId 模板ID
     * @param queueDate  队列日期
     * @return 队列实例
     */
    public QueueInstance getOrCreateInstance(Long storeId, Long templateId, LocalDate queueDate) {
        QueueInstance instance = getBaseMapper().selectByStoreAndDate(storeId, templateId, queueDate);
        if (instance != null) {
            return instance;
        }
        String lockKey = QueueConstants.TAKE_LOCK_KEY_PREFIX + "instance:" + storeId + ":" + templateId + ":" + queueDate;
        return distributedLockUtil.executeWithLock(
                lockKey,
                QueueConstants.LOCK_WAIT_SECONDS,
                QueueConstants.LOCK_LEASE_SECONDS,
                () -> doCreateInstance(storeId, templateId, queueDate)
        );
    }

    private QueueInstance doCreateInstance(Long storeId, Long templateId, LocalDate queueDate) {
        QueueInstance instance = getBaseMapper().selectByStoreAndDate(storeId, templateId, queueDate);
        if (instance != null) {
            return instance;
        }
        QueueInstance newInstance = new QueueInstance();
        newInstance.setStoreId(storeId);
        newInstance.setTemplateId(templateId);
        newInstance.setQueueDate(queueDate);
        newInstance.setStatus(QueueStatus.OPEN.getCode());
        newInstance.setWaitCount(0);
        getBaseMapper().insert(newInstance);
        log.info("创建队列实例，storeId={}，templateId={}，queueDate={}", storeId, templateId, queueDate);
        return newInstance;
    }

    /**
     * 根据ID查询队列实例
     *
     * @param instanceId 实例ID
     * @return 队列实例
     */
    public QueueInstance getByIdSafe(Long instanceId) {
        return getById(instanceId);
    }
}
