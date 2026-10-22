package com.queue.ticketing.modules.store.converter;

import com.queue.ticketing.modules.store.dto.StoreDTO;
import com.queue.ticketing.modules.store.entity.Store;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * 门店对象转换器
 *
 * @author yh
 * @date 2026-10-22
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StoreConverter {

    /**
     * DTO 转 Entity
     *
     * @param dto 门店DTO
     * @return 门店实体
     */
    @Mapping(target = "status", constant = "1")
    Store toEntity(StoreDTO dto);
}
