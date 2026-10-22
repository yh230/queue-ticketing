package com.queue.ticketing.modules.notify.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.queue.ticketing.modules.notify.entity.Subscription;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知订阅 Mapper
 *
 * @author yh
 * @date 2026-10-22
 */
@Mapper
public interface SubscriptionMapper extends BaseMapper<Subscription> {
}
