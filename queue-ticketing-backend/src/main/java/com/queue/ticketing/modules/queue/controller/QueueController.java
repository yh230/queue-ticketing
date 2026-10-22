package com.queue.ticketing.modules.queue.controller;

import com.queue.ticketing.common.annotation.RequireAuth;
import com.queue.ticketing.common.result.Result;
import com.queue.ticketing.modules.auth.context.LoginContext;
import com.queue.ticketing.modules.auth.dto.LoginUser;
import com.queue.ticketing.modules.queue.redis.QueueCallService;
import com.queue.ticketing.modules.queue.redis.QueueQueryService;
import com.queue.ticketing.modules.queue.redis.QueueTakeService;
import com.queue.ticketing.modules.queue.vo.QueueStatusVO;
import com.queue.ticketing.modules.ticket.vo.TicketVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Redis 排队接口
 *
 * @author yh
 * @date 2026-10-22
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/queue")
public class QueueController {

    private final QueueTakeService queueTakeService;
    private final QueueCallService queueCallService;
    private final QueueQueryService queueQueryService;

    /**
     * C端：取号
     *
     * @param requestId 幂等请求ID
     * @param request   取号请求
     * @return 取号结果
     */
    @RequireAuth(allowClient = true)
    @PostMapping("/take")
    public Result<TicketVO> take(@RequestHeader(value = "Idempotency-Key", required = false) String requestId,
                                 @Valid @RequestBody TakeRequest request) {
        LoginUser loginUser = LoginContext.get();
        String idempotentKey = requestId == null || requestId.isBlank()
                ? UUID.randomUUID().toString() : requestId;
        return Result.success(queueTakeService.take(
                request.getStoreId(),
                request.getTemplateId(),
                loginUser.getUserId(),
                idempotentKey,
                request.getUserPhone(),
                request.getSource()));
    }

    /**
     * C端：查询排队进度
     *
     * @param storeId    门店ID
     * @param templateId 队列模板ID
     * @param ticketNo   号码文本
     * @return 进度
     */
    @RequireAuth(allowClient = true)
    @GetMapping("/progress")
    public Result<TicketVO> progress(@RequestParam Long storeId,
                                     @RequestParam Long templateId,
                                     @RequestParam String ticketNo) {
        return Result.success(queueQueryService.progress(storeId, templateId, ticketNo));
    }

    /**
     * C端：查询我的当前排队
     *
     * @param storeId 门店ID
     * @return 当前取号单
     */
    @RequireAuth(allowClient = true)
    @GetMapping("/my")
    public Result<TicketVO> my(@RequestParam Long storeId) {
        LoginUser loginUser = LoginContext.get();
        return Result.success(queueQueryService.my(storeId, loginUser.getUserId()));
    }

    /**
     * C端：取消取号
     *
     * @param request 取消请求
     * @return 空
     */
    @RequireAuth(allowClient = true)
    @PostMapping("/cancel")
    public Result<Void> cancel(@Valid @RequestBody CancelRequest request) {
        LoginUser loginUser = LoginContext.get();
        String uid = "CLIENT".equals(loginUser.getUserType()) ? loginUser.getUserId() : "";
        queueCallService.cancel(request.getStoreId(), request.getTemplateId(), uid, request.getTicketNo());
        return Result.success();
    }

    /**
     * B端：叫号
     *
     * @param request 叫号请求
     * @return 被叫号码
     */
    @RequireAuth
    @PostMapping("/call")
    public Result<TicketVO> call(@Valid @RequestBody CallRequest request) {
        return Result.success(queueCallService.call(
                request.getStoreId(), request.getTemplateId(), request.getCounterId()));
    }

    /**
     * B端：完成服务
     *
     * @param request 完成请求
     * @return 空
     */
    @RequireAuth
    @PostMapping("/finish")
    public Result<Void> finish(@Valid @RequestBody TicketNoRequest request) {
        queueCallService.finish(request.getStoreId(), request.getTemplateId(), "", request.getTicketNo());
        return Result.success();
    }

    /**
     * B端：过号
     *
     * @param request 过号请求
     * @return 空
     */
    @RequireAuth
    @PostMapping("/miss")
    public Result<Void> miss(@Valid @RequestBody TicketNoRequest request) {
        queueCallService.miss(request.getStoreId(), request.getTemplateId(), request.getTicketNo());
        return Result.success();
    }

    /**
     * B端：队列总览
     *
     * @param storeId 门店ID
     * @return 队列状态列表
     */
    @RequireAuth
    @GetMapping("/overview")
    public Result<List<QueueStatusVO>> overview(@RequestParam Long storeId) {
        return Result.success(queueQueryService.overview(storeId));
    }

    /**
     * 取号请求
     */
    @Data
    public static class TakeRequest {
        @NotNull(message = "门店ID不能为空")
        private Long storeId;
        @NotNull(message = "队列模板ID不能为空")
        private Long templateId;
        private String userPhone;
        private String source = "MINI_PROGRAM";
    }

    /**
     * 取消请求
     */
    @Data
    public static class CancelRequest {
        @NotNull(message = "门店ID不能为空")
        private Long storeId;
        @NotNull(message = "队列模板ID不能为空")
        private Long templateId;
        @NotBlank(message = "号码不能为空")
        private String ticketNo;
    }

    /**
     * 叫号请求
     */
    @Data
    public static class CallRequest {
        @NotNull(message = "门店ID不能为空")
        private Long storeId;
        @NotNull(message = "队列模板ID不能为空")
        private Long templateId;
        private Long counterId;
    }

    /**
     * 号码请求
     */
    @Data
    public static class TicketNoRequest {
        @NotNull(message = "门店ID不能为空")
        private Long storeId;
        @NotNull(message = "队列模板ID不能为空")
        private Long templateId;
        @NotBlank(message = "号码不能为空")
        private String ticketNo;
    }
}
