package com.queue.ticketing.modules.queue.mq;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 本地消息表 Mapper
 *
 * @author yh
 * @date 2026-10-22
 */
@Mapper
public interface LocalMessageMapper extends BaseMapper<LocalMessage> {
}
