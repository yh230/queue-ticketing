package com.queue.ticketing.modules.queue.converter;

import com.queue.ticketing.modules.queue.dto.QueueTemplateDTO;
import com.queue.ticketing.modules.queue.entity.QueueTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * 队列模板对象转换器
 *
 * @author yh
 * @date 2026-10-22
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface QueueTemplateConverter {

    /**
     * DTO 转 Entity
     *
     * @param dto 队列模板DTO
     * @return 队列模板实体
     */
    @Mapping(target = "status", constant = "1")
    QueueTemplate toEntity(QueueTemplateDTO dto);
}
