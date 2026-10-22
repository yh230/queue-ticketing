package com.queue.ticketing.modules.store.controller;

import com.queue.ticketing.common.result.Result;
import com.queue.ticketing.common.annotation.RequireAuth;
import com.queue.ticketing.modules.store.converter.StoreConverter;
import com.queue.ticketing.modules.store.dto.StoreDTO;
import com.queue.ticketing.modules.store.entity.Store;
import com.queue.ticketing.modules.store.mapper.StoreMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 门店接口
 *
 * @author yh
 * @date 2026-10-22
 */
@RequireAuth
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreMapper storeMapper;
    private final StoreConverter storeConverter;

    /**
     * 创建门店
     *
     * @param dto 门店信息
     * @return 门店ID
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody StoreDTO dto) {
        Store store = storeConverter.toEntity(dto);
        storeMapper.insert(store);
        return Result.success(store.getId());
    }

    /**
     * 查询商户下门店列表
     *
     * @param merchantId 商户ID
     * @return 门店列表
     */
    @GetMapping("/merchant/{merchantId}")
    public Result<List<Store>> listByMerchant(@PathVariable Long merchantId) {
        List<Store> list = storeMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Store>()
                        .eq(Store::getMerchantId, merchantId)
                        .eq(Store::getStatus, 1)
        );
        return Result.success(list);
    }
}
