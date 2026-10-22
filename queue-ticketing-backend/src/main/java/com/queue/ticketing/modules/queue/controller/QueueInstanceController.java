package com.queue.ticketing.modules.queue.controller;

import com.queue.ticketing.common.result.Result;
import com.queue.ticketing.common.annotation.RequireAuth;
import com.queue.ticketing.modules.queue.entity.QueueInstance;
import com.queue.ticketing.modules.queue.service.QueueInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 队列实例接口
 *
 * @author yh
 * @date 2026-10-22
 */
@RequireAuth
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/queue-instances")
public class QueueInstanceController {

    private final QueueInstanceService queueInstanceService;

    /**
     * 获取或创建队列实例
     *
     * @param storeId    门店ID
     * @param templateId 模板ID
     * @param queueDate  日期
     * @return 队列实例
     */
    @GetMapping
    public Result<QueueInstance> getOrCreate(
            @RequestParam Long storeId,
            @RequestParam Long templateId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate queueDate) {
        LocalDate date = queueDate == null ? LocalDate.now() : queueDate;
        return Result.success(queueInstanceService.getOrCreateInstance(storeId, templateId, date));
    }
}
