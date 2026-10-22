package com.queue.ticketing.modules.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.queue.ticketing.modules.store.entity.CounterQueueTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 柜台与队列模板关联 Mapper
 *
 * @author yh
 * @date 2026-10-22
 */
@Mapper
public interface CounterQueueTemplateMapper extends BaseMapper<CounterQueueTemplate> {

    /**
     * 查询配置了指定队列模板的柜台ID列表
     *
     * @param templateId 队列模板ID
     * @return 柜台ID列表
     */
    @Select("SELECT counter_id FROM counter_queue_template " +
            "WHERE queue_template_id = #{templateId} AND deleted = 0")
    List<Long> selectCounterIdsByTemplateId(@Param("templateId") Long templateId);
}
