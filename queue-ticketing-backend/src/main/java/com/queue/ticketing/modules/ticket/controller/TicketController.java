package com.queue.ticketing.modules.ticket.controller;

import com.queue.ticketing.common.result.Result;
import com.queue.ticketing.common.annotation.Idempotent;
import com.queue.ticketing.common.annotation.RequireAuth;
import com.queue.ticketing.modules.auth.context.LoginContext;
import com.queue.ticketing.modules.auth.dto.LoginUser;
import com.queue.ticketing.modules.queue.vo.QueueStatusVO;
import com.queue.ticketing.modules.ticket.dto.CallTicketRequest;
import com.queue.ticketing.modules.ticket.dto.CancelTicketRequest;
import com.queue.ticketing.modules.ticket.dto.MissTicketRequest;
import com.queue.ticketing.modules.ticket.dto.TakeTicketRequest;
import com.queue.ticketing.modules.ticket.service.TicketService;
import com.queue.ticketing.modules.ticket.vo.TicketVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 取号单接口
 *
 * @author yh
 * @date 2026-10-22
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    /**
     * C端：扫码取号
     *
     * @param request 取号请求
     * @return 取号单信息
     */
    @RequireAuth(allowClient = true)
    @PostMapping("/take")
    @Idempotent(prefix = "take-ticket", ttlSeconds = 300, message = "请勿重复取号")
    public Result<TicketVO> takeTicket(@Valid @RequestBody TakeTicketRequest request) {
        LoginUser loginUser = LoginContext.get();
        request.setUserId(String.valueOf(loginUser.getUserId()));
        return Result.success(ticketService.takeTicket(request));
    }

    /**
     * C端：查询取号单详情
     *
     * @param ticketId 取号单ID
     * @return 取号单信息
     */
    @RequireAuth(allowClient = true)
    @GetMapping("/{ticketId}")
    public Result<TicketVO> getTicket(@PathVariable Long ticketId) {
        return Result.success(ticketService.getTicketDetail(ticketId));
    }

    /**
     * C端：查询当前用户进行中的取号单
     *
     * @param storeId    门店ID
     * @param templateId 队列模板ID
     * @return 进行中的取号单，无则返回 null
     */
    @RequireAuth(allowClient = true)
    @GetMapping("/active")
    public Result<TicketVO> getActiveTicket(@RequestParam Long storeId, @RequestParam Long templateId) {
        LoginUser loginUser = LoginContext.get();
        return Result.success(ticketService.getActiveTicket(storeId, templateId, loginUser.getUserId()));
    }

    /**
     * 查询队列实时状态
     *
     * @param queueInstanceId 队列实例ID
     * @return 队列状态
     */
    @RequireAuth(allowClient = true)
    @GetMapping("/queue/{queueInstanceId}/status")
    public Result<QueueStatusVO> getQueueStatus(@PathVariable Long queueInstanceId) {
        return Result.success(ticketService.getQueueStatus(queueInstanceId));
    }

    /**
     * C端：取消取号
     *
     * @param request 取消请求
     * @return 空
     */
    @RequireAuth(allowClient = true)
    @PostMapping("/cancel")
    public Result<Void> cancelTicket(@Valid @RequestBody CancelTicketRequest request) {
        LoginUser loginUser = LoginContext.get();
        request.setUserId(String.valueOf(loginUser.getUserId()));
        ticketService.cancelTicket(request);
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
    public Result<TicketVO> callTicket(@Valid @RequestBody CallTicketRequest request) {
        return Result.success(ticketService.callTicket(request));
    }

    /**
     * B端：过号
     *
     * @param request 过号请求
     * @return 过号后信息
     */
    @RequireAuth
    @PostMapping("/miss")
    public Result<TicketVO> missTicket(@Valid @RequestBody MissTicketRequest request) {
        return Result.success(ticketService.missTicket(request));
    }

    /**
     * B端：标记服务完成
     *
     * @param ticketId 取号单ID
     * @return 完成信息
     */
    @RequireAuth
    @PostMapping("/{ticketId}/serve")
    public Result<TicketVO> serveTicket(@PathVariable Long ticketId) {
        return Result.success(ticketService.serveTicket(ticketId));
    }
}
