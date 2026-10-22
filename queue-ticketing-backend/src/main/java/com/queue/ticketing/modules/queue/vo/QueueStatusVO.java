package com.queue.ticketing.modules.queue.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 队列实时状态
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
public class QueueStatusVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 队列实例ID
     */
    private Long queueInstanceId;

    /**
     * 队列状态：OPEN/PAUSED/CLOSED
     */
    private String status;

    /**
     * 当前等待人数
     */
    private Integer waitCount;

    /**
     * 当前叫号 ticket ID
     */
    private Long currentTicketId;

    /**
     * 当前叫号号码
     */
    private String currentTicketNo;

    /**
     * 等待列表（按排队顺序）
     */
    private List<WaitingTicketVO> waitingTickets;
}
