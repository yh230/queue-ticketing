package com.queue.ticketing.interfaces.controller;

import com.queue.ticketing.common.Result;
import com.queue.ticketing.common.annotation.RequireAuth;
import com.queue.ticketing.modules.store.converter.MerchantConverter;
import com.queue.ticketing.modules.store.dto.MerchantDTO;
import com.queue.ticketing.modules.store.entity.Merchant;
import com.queue.ticketing.modules.store.mapper.MerchantMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商户接口
 *
 * @author yh
 * @date 2026-10-22
 */
@RequireAuth
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/merchants")
public class MerchantController {

    private final MerchantMapper merchantMapper;
    private final MerchantConverter merchantConverter;

    /**
     * 创建商户
     *
     * @param dto 商户信息
     * @return 商户ID
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody MerchantDTO dto) {
        Merchant merchant = merchantConverter.toEntity(dto);
        merchantMapper.insert(merchant);
        return Result.success(merchant.getId());
    }

    /**
     * 查询商户
     *
     * @param id 商户ID
     * @return 商户信息
     */
    @GetMapping("/{id}")
    public Result<Merchant> get(@PathVariable Long id) {
        return Result.success(merchantMapper.selectById(id));
    }
}
