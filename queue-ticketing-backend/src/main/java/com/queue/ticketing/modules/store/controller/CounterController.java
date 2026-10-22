package com.queue.ticketing.modules.store.controller;

import com.queue.ticketing.common.result.Result;
import com.queue.ticketing.common.annotation.RequireAuth;
import com.queue.ticketing.modules.store.converter.CounterConverter;
import com.queue.ticketing.modules.store.dto.CounterDTO;
import com.queue.ticketing.modules.store.entity.Counter;
import com.queue.ticketing.modules.store.entity.CounterQueueTemplate;
import com.queue.ticketing.modules.store.mapper.CounterMapper;
import com.queue.ticketing.modules.store.mapper.CounterQueueTemplateMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 柜台接口
 *
 * @author yh
 * @date 2026-10-22
 */
@RequireAuth
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/counters")
public class CounterController {

    private final CounterMapper counterMapper;
    private final CounterQueueTemplateMapper counterQueueTemplateMapper;
    private final CounterConverter counterConverter;

    /**
     * 创建柜台
     *
     * @param dto 柜台信息
     * @return 柜台ID
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> create(@Valid @RequestBody CounterDTO dto) {
        Counter counter = counterConverter.toEntity(dto);
        counterMapper.insert(counter);

        if (dto.getQueueTemplateIds() != null) {
            for (Long templateId : dto.getQueueTemplateIds()) {
                CounterQueueTemplate relation = new CounterQueueTemplate();
                relation.setCounterId(counter.getId());
                relation.setQueueTemplateId(templateId);
                counterQueueTemplateMapper.insert(relation);
            }
        }
        return Result.success(counter.getId());
    }

    /**
     * 查询门店下柜台
     *
     * @param storeId 门店ID
     * @return 柜台列表
     */
    @GetMapping("/store/{storeId}")
    public Result<List<Counter>> listByStore(@PathVariable Long storeId) {
        List<Counter> list = counterMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Counter>()
                        .eq(Counter::getStoreId, storeId)
                        .eq(Counter::getStatus, 1)
        );
        return Result.success(list);
    }
}
