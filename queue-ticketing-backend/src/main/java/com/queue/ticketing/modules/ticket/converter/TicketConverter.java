package com.queue.ticketing.modules.ticket.converter;

import com.queue.ticketing.modules.ticket.entity.Ticket;
import com.queue.ticketing.modules.ticket.vo.TicketVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mappings;

/**
 * 取号单对象转换器
 *
 * @author yh
 * @date 2026-10-22
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TicketConverter {

    /**
     * Entity 转 VO（基础字段）
     *
     * @param ticket 取号单实体
     * @return 取号单VO
     */
    @Mappings({
            @Mapping(target = "statusDesc", ignore = true),
            @Mapping(target = "aheadCount", ignore = true),
            @Mapping(target = "estimatedWaitMinutes", ignore = true)
    })
    TicketVO toVo(Ticket ticket);
}
