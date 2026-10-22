package com.queue.ticketing.modules.queue.controller;

import com.queue.ticketing.common.result.Result;
import com.queue.ticketing.common.annotation.RequireAuth;
import com.queue.ticketing.modules.queue.converter.QueueTemplateConverter;
import com.queue.ticketing.modules.queue.dto.QueueTemplateDTO;
import com.queue.ticketing.modules.queue.entity.QueueTemplate;
import com.queue.ticketing.modules.queue.mapper.QueueTemplateMapper;
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
 * 队列模板接口
 *
 * @author yh
 * @date 2026-10-22
 */
@RequireAuth
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/queue-templates")
public class QueueTemplateController {

    private final QueueTemplateMapper queueTemplateMapper;
    private final QueueTemplateConverter queueTemplateConverter;

    /**
     * 创建队列模板
     *
     * @param dto 模板信息
     * @return 模板ID
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody QueueTemplateDTO dto) {
        QueueTemplate template = queueTemplateConverter.toEntity(dto);
        queueTemplateMapper.insert(template);
        return Result.success(template.getId());
    }

    /**
     * 查询商户下队列模板
     *
     * @param merchantId 商户ID
     * @return 模板列表
     */
    @GetMapping("/merchant/{merchantId}")
    public Result<List<QueueTemplate>> listByMerchant(@PathVariable Long merchantId) {
        List<QueueTemplate> list = queueTemplateMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<QueueTemplate>()
                        .eq(QueueTemplate::getMerchantId, merchantId)
                        .eq(QueueTemplate::getStatus, 1)
        );
        return Result.success(list);
    }
}
