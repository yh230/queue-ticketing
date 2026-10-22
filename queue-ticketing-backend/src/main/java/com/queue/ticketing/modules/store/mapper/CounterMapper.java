package com.queue.ticketing.modules.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.queue.ticketing.modules.store.entity.Counter;
import org.apache.ibatis.annotations.Mapper;

/**
 * 柜台 Mapper
 *
 * @author yh
 * @date 2026-10-22
 */
@Mapper
public interface CounterMapper extends BaseMapper<Counter> {
}
