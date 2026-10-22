package com.queue.ticketing.modules.store.converter;

import com.queue.ticketing.modules.store.dto.CounterDTO;
import com.queue.ticketing.modules.store.entity.Counter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * 柜台对象转换器
 *
 * @author yh
 * @date 2026-10-22
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CounterConverter {

    /**
     * DTO 转 Entity（不复制 queueTemplateIds）
     *
     * @param dto 柜台DTO
     * @return 柜台实体
     */
    @Mapping(target = "status", constant = "1")
    Counter toEntity(CounterDTO dto);
}
