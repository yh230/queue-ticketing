package com.queue.ticketing.modules.ticket.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.queue.ticketing.common.BizException;
import com.queue.ticketing.common.ErrorCodeEnum;
import com.queue.ticketing.common.QueueConstants;
import com.queue.ticketing.common.event.DomainEventPublisher;
import com.queue.ticketing.common.util.DistributedLockUtil;
import com.queue.ticketing.modules.queue.dto.QueueRule;
import com.queue.ticketing.modules.queue.entity.QueueInstance;
import com.queue.ticketing.modules.queue.entity.QueueTemplate;
import com.queue.ticketing.modules.queue.enums.QueueStatus;
import com.queue.ticketing.modules.queue.service.QueueInstanceService;
import com.queue.ticketing.modules.queue.service.QueueTemplateService;
import com.queue.ticketing.modules.ticket.converter.TicketConverter;
import com.queue.ticketing.modules.ticket.dto.CallTicketRequest;
import com.queue.ticketing.modules.ticket.dto.CancelTicketRequest;
import com.queue.ticketing.modules.ticket.dto.MissTicketRequest;
import com.queue.ticketing.modules.ticket.dto.TakeTicketRequest;
import com.queue.ticketing.modules.ticket.entity.Ticket;
import com.queue.ticketing.modules.ticket.enums.TicketStatus;
import com.queue.ticketing.modules.ticket.event.TicketCalledEvent;
import com.queue.ticketing.modules.ticket.event.TicketCancelledEvent;
import com.queue.ticketing.modules.ticket.event.TicketCreatedEvent;
import com.queue.ticketing.modules.ticket.event.TicketMissedEvent;
import com.queue.ticketing.modules.ticket.event.TicketServedEvent;
import com.queue.ticketing.modules.ticket.mapper.TicketMapper;
import com.queue.ticketing.modules.ticket.vo.TicketVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 取号单服务
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService extends ServiceImpl<TicketMapper, Ticket> {

    private final TicketNoGenerator ticketNoGenerator;
    private final DistributedLockUtil distributedLockUtil;
    private final QueueInstanceService queueInstanceService;
    private final QueueTemplateService queueTemplateService;
    private final DomainEventPublisher domainEventPublisher;
    private final TicketConverter ticketConverter;

    private TicketService self;

    @Autowired
    public void setSelf(@Lazy TicketService self) {
        this.self = self;
    }

    /**
     * 取号
     *
     * @param request 取号请求
     * @return 取号单VO
     */
    public TicketVO takeTicket(TakeTicketRequest request) {
        QueueTemplate template = queueTemplateService.getByIdSafe(request.getTemplateId());
        QueueRule rule = queueTemplateService.parseRule(template);

        LocalDate queueDate = request.getQueueDate() == null ? LocalDate.now() : request.getQueueDate();
        if (queueDate.isBefore(LocalDate.now())) {
            throw new BizException(ErrorCodeEnum.PARAM_ERROR, "取号日期不能早于今天");
        }
        if (queueDate.isAfter(LocalDate.now().plusDays(7))) {
            throw new BizException(ErrorCodeEnum.PARAM_ERROR, "最多可预约未来 7 天");
        }

        QueueInstance instance = queueInstanceService.getOrCreateInstance(request.getStoreId(), request.getTemplateId(), queueDate);

        return distributedLockUtil.executeWithLock(
                QueueConstants.TAKE_LOCK_KEY_PREFIX + instance.getId(),
                QueueConstants.LOCK_WAIT_SECONDS,
                QueueConstants.LOCK_LEASE_SECONDS,
                () -> self.doTakeTicket(request, instance, template, rule)
        );
    }

    /**
     * 取号业务逻辑（在锁内和事务内执行）
     *
     * @param request  取号请求
     * @param instance 队列实例
     * @param template 队列模板
     * @param rule     队列规则
     * @return 取号单VO
     */
    @Transactional(rollbackFor = Exception.class)
    public TicketVO doTakeTicket(TakeTicketRequest request, QueueInstance instance, QueueTemplate template, QueueRule rule) {
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new BizException(ErrorCodeEnum.PARAM_ERROR, "用户ID不能为空");
        }

        // 检查队列状态
        if (QueueStatus.PAUSED.getCode().equals(instance.getStatus())) {
            throw new BizException(ErrorCodeEnum.QUEUE_PAUSED);
        }
        if (QueueStatus.CLOSED.getCode().equals(instance.getStatus())) {
            throw new BizException(ErrorCodeEnum.QUEUE_CLOSED);
        }

        // 检查重复取号
        List<Ticket> activeTickets = getBaseMapper().selectActiveByUser(instance.getId(), request.getUserId());
        if (!activeTickets.isEmpty()) {
            throw new BizException(ErrorCodeEnum.TICKET_DUPLICATE);
        }

        // 检查容量
        Long waitingCount = getBaseMapper().countByInstanceAndStatus(instance.getId(), TicketStatus.WAITING.getCode());
        if (waitingCount >= rule.getCapacity()) {
            throw new BizException(ErrorCodeEnum.QUEUE_FULL);
        }

        // 生成号码
        String ticketNo = ticketNoGenerator.nextTicketNo(instance.getId(), template.getPrefix());

        // 保存取号单
        Ticket ticket = new Ticket();
        ticket.setStoreId(request.getStoreId());
        ticket.setQueueInstanceId(instance.getId());
        ticket.setTicketNo(ticketNo);
        ticket.setUserId(request.getUserId());
        ticket.setUserPhone(request.getUserPhone());
        ticket.setStatus(TicketStatus.WAITING.getCode());
        ticket.setSource(request.getSource());
        ticket.setMissedCount(0);
        ticket.setIdempotencyKey(request.getIdempotencyKey());
        getBaseMapper().insert(ticket);

        // 更新等待人数（取号不更新当前叫号）
        queueInstanceService.getBaseMapper().updateCurrentTicket(instance.getId(), null, 1);

        // 发布事件
        domainEventPublisher.publish(new TicketCreatedEvent(ticket.getId(), instance.getId(), ticketNo, request.getUserId()));

        log.info("取号成功，ticketId={}，ticketNo={}，queueInstanceId={}", ticket.getId(), ticketNo, instance.getId());
        return buildTicketVO(ticket, instance.getId());
    }

    /**
     * 叫号
     *
     * @param request 叫号请求
     * @return 被叫号码VO
     */
    public TicketVO callTicket(CallTicketRequest request) {
        return distributedLockUtil.executeWithLock(
                QueueConstants.CALL_LOCK_KEY_PREFIX + request.getQueueInstanceId() + ":" + request.getCounterId(),
                QueueConstants.LOCK_WAIT_SECONDS,
                QueueConstants.LOCK_LEASE_SECONDS,
                () -> self.doCallTicket(request)
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public TicketVO doCallTicket(CallTicketRequest request) {
        QueueInstance instance = queueInstanceService.getByIdSafe(request.getQueueInstanceId());
        if (instance == null) {
            throw new BizException(ErrorCodeEnum.QUEUE_NOT_FOUND);
        }

        // 循环尝试叫号，支持多柜台并发
        int maxRetry = 3;
        for (int i = 0; i < maxRetry; i++) {
            Ticket ticket = getBaseMapper().selectOldestWaiting(instance.getId());
            if (ticket == null) {
                throw new BizException(ErrorCodeEnum.CALL_TICKET_FAILED);
            }

            int rows = getBaseMapper().updateStatusByOldStatus(
                    ticket.getId(),
                    TicketStatus.CALLED.getCode(),
                    TicketStatus.WAITING.getCode(),
                    request.getCounterId(),
                    LocalDateTime.now()
            );
            if (rows > 0) {
                ticket = getById(ticket.getId());
                queueInstanceService.getBaseMapper().updateCurrentTicket(instance.getId(), ticket.getId(), -1);
                domainEventPublisher.publish(new TicketCalledEvent(
                        ticket.getId(), instance.getId(), ticket.getTicketNo(), ticket.getUserId(), request.getCounterId()
                ));
                log.info("叫号成功，ticketId={}，ticketNo={}，counterId={}", ticket.getId(), ticket.getTicketNo(), request.getCounterId());
                return buildTicketVO(ticket, instance.getId());
            }
            log.warn("叫号乐观锁冲突，重试，ticketId={}", ticket.getId());
        }

        throw new BizException(ErrorCodeEnum.CALL_TICKET_FAILED);
    }

    /**
     * 过号
     *
     * @param request 过号请求
     * @return 过号后VO
     */
    public TicketVO missTicket(MissTicketRequest request) {
        Ticket ticket = getById(request.getTicketId());
        if (ticket == null) {
            throw new BizException(ErrorCodeEnum.TICKET_NOT_FOUND);
        }

        return distributedLockUtil.executeWithLock(
                QueueConstants.CALL_LOCK_KEY_PREFIX + ticket.getQueueInstanceId(),
                QueueConstants.LOCK_WAIT_SECONDS,
                QueueConstants.LOCK_LEASE_SECONDS,
                () -> self.doMissTicket(request.getTicketId())
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public TicketVO doMissTicket(Long ticketId) {
        Ticket ticket = getById(ticketId);
        if (ticket == null) {
            throw new BizException(ErrorCodeEnum.TICKET_NOT_FOUND);
        }
        if (!TicketStatus.CALLED.getCode().equals(ticket.getStatus())) {
            throw new BizException(ErrorCodeEnum.TICKET_STATUS_ILLEGAL, "只有已叫号状态才可过号");
        }

        QueueInstance instance = queueInstanceService.getByIdSafe(ticket.getQueueInstanceId());
        QueueTemplate template = queueTemplateService.getByIdSafe(instance.getTemplateId());
        QueueRule rule = queueTemplateService.parseRule(template);

        int newMissedCount = ticket.getMissedCount() + 1;

        // 超过最大过号次数则作废
        if (rule.getMaxMissedCount() != null && newMissedCount > rule.getMaxMissedCount()) {
            ticket.setStatus(TicketStatus.CANCELLED.getCode());
            updateById(ticket);
            domainEventPublisher.publish(new TicketCancelledEvent(ticket.getId(), instance.getId(), ticket.getTicketNo(), ticket.getUserId()));
            log.info("过号次数超限作废，ticketId={}", ticket.getId());
            return buildTicketVO(ticket, instance.getId());
        }

        // 过号后顺延重新排队
        ticket.setStatus(TicketStatus.MISSED.getCode());
        ticket.setMissedCount(newMissedCount);
        updateById(ticket);

        if (rule.getMissedDeferCount() != null && rule.getMissedDeferCount() > 0) {
            // 简单实现：直接改回 WAITING，由队列顺序自然排在后面
            ticket.setStatus(TicketStatus.WAITING.getCode());
            // 更新时间排在队尾
            ticket.setUpdatedAt(LocalDateTime.now());
            updateById(ticket);
            queueInstanceService.getBaseMapper().updateCurrentTicket(instance.getId(), null, 1);
            log.info("过号顺延排队，ticketId={}，missedCount={}", ticket.getId(), newMissedCount);
        }

        domainEventPublisher.publish(new TicketMissedEvent(
                ticket.getId(), instance.getId(), ticket.getTicketNo(), ticket.getUserId(), newMissedCount
        ));

        return buildTicketVO(ticket, instance.getId());
    }

    /**
     * 取消取号
     *
     * @param request 取消请求
     */
    public void cancelTicket(CancelTicketRequest request) {
        Ticket ticket = getById(request.getTicketId());
        if (ticket == null) {
            throw new BizException(ErrorCodeEnum.TICKET_NOT_FOUND);
        }

        distributedLockUtil.<Void>executeWithLock(
                QueueConstants.TAKE_LOCK_KEY_PREFIX + ticket.getQueueInstanceId(),
                QueueConstants.LOCK_WAIT_SECONDS,
                QueueConstants.LOCK_LEASE_SECONDS,
                () -> {
                    self.doCancelTicket(request.getTicketId(), request.getUserId());
                    return null;
                }
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void doCancelTicket(Long ticketId, String userId) {
        Ticket ticket = getById(ticketId);
        if (ticket == null) {
            throw new BizException(ErrorCodeEnum.TICKET_NOT_FOUND);
        }

        if (userId != null && !userId.equals(ticket.getUserId())) {
            throw new BizException(ErrorCodeEnum.FORBIDDEN, "无权取消该取号单");
        }

        if (!TicketStatus.WAITING.getCode().equals(ticket.getStatus())
                && !TicketStatus.CALLED.getCode().equals(ticket.getStatus())) {
            throw new BizException(ErrorCodeEnum.TICKET_STATUS_ILLEGAL, "当前状态不允许取消");
        }

        String originalStatus = ticket.getStatus();
        ticket.setStatus(TicketStatus.CANCELLED.getCode());
        updateById(ticket);

        QueueInstance instance = queueInstanceService.getByIdSafe(ticket.getQueueInstanceId());
        if (TicketStatus.WAITING.getCode().equals(originalStatus)) {
            queueInstanceService.getBaseMapper().updateCurrentTicket(instance.getId(), null, -1);
        } else if (TicketStatus.CALLED.getCode().equals(originalStatus)
                && ticket.getId().equals(instance.getCurrentTicketId())) {
            queueInstanceService.getBaseMapper().updateCurrentTicket(instance.getId(), null, 0);
        }

        domainEventPublisher.publish(new TicketCancelledEvent(
                ticket.getId(), instance.getId(), ticket.getTicketNo(), ticket.getUserId()
        ));

        log.info("取消取号，ticketId={}", ticket.getId());
    }

    /**
     * 标记服务完成
     *
     * @param ticketId 取号单ID
     * @return VO
     */
    public TicketVO serveTicket(Long ticketId) {
        Ticket ticket = getById(ticketId);
        if (ticket == null) {
            throw new BizException(ErrorCodeEnum.TICKET_NOT_FOUND);
        }

        return distributedLockUtil.executeWithLock(
                QueueConstants.CALL_LOCK_KEY_PREFIX + ticket.getQueueInstanceId(),
                QueueConstants.LOCK_WAIT_SECONDS,
                QueueConstants.LOCK_LEASE_SECONDS,
                () -> self.doServeTicket(ticketId)
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public TicketVO doServeTicket(Long ticketId) {
        Ticket ticket = getById(ticketId);
        if (ticket == null) {
            throw new BizException(ErrorCodeEnum.TICKET_NOT_FOUND);
        }
        if (!TicketStatus.CALLED.getCode().equals(ticket.getStatus())) {
            throw new BizException(ErrorCodeEnum.TICKET_STATUS_ILLEGAL, "只有已叫号状态才可标记完成");
        }

        ticket.setStatus(TicketStatus.SERVED.getCode());
        ticket.setServedAt(LocalDateTime.now());
        updateById(ticket);

        domainEventPublisher.publish(new TicketServedEvent(
                ticket.getId(), ticket.getQueueInstanceId(), ticket.getTicketNo(), ticket.getUserId(), ticket.getCounterId()
        ));

        log.info("服务完成，ticketId={}", ticket.getId());
        return buildTicketVO(ticket, ticket.getQueueInstanceId());
    }

    /**
     * 查询取号单详情
     *
     * @param ticketId 取号单ID
     * @return VO
     */
    public TicketVO getTicketDetail(Long ticketId) {
        Ticket ticket = getById(ticketId);
        if (ticket == null) {
            throw new BizException(ErrorCodeEnum.TICKET_NOT_FOUND);
        }
        return buildTicketVO(ticket, ticket.getQueueInstanceId());
    }

    /**
     * 构建 VO
     *
     * @param ticket          取号单
     * @param queueInstanceId 队列实例ID
     * @return VO
     */
    private TicketVO buildTicketVO(Ticket ticket, Long queueInstanceId) {
        TicketVO vo = ticketConverter.toVo(ticket);

        TicketStatus status = TicketStatus.of(ticket.getStatus());
        if (status != null) {
            vo.setStatusDesc(status.getDesc());
        }

        // 前方等待人数（按更新时间排序，过号重新排队会排在队尾）
        if (TicketStatus.WAITING.getCode().equals(ticket.getStatus())) {
            Long ahead = getBaseMapper().selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Ticket>()
                    .eq(Ticket::getQueueInstanceId, queueInstanceId)
                    .eq(Ticket::getStatus, TicketStatus.WAITING.getCode())
                    .lt(Ticket::getUpdatedAt, ticket.getUpdatedAt()));
            vo.setAheadCount(ahead.intValue());
            vo.setEstimatedWaitMinutes(ahead.intValue() * 5);
        }

        return vo;
    }
}
