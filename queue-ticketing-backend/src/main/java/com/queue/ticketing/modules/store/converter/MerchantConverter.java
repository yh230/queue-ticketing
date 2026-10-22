package com.queue.ticketing.modules.store.converter;

import com.queue.ticketing.modules.store.dto.MerchantDTO;
import com.queue.ticketing.modules.store.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * 商户对象转换器
 *
 * @author yh
 * @date 2026-10-22
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MerchantConverter {

    /**
     * DTO 转 Entity
     *
     * @param dto 商户DTO
     * @return 商户实体
     */
    @Mapping(target = "status", constant = "1")
    Merchant toEntity(MerchantDTO dto);
}
