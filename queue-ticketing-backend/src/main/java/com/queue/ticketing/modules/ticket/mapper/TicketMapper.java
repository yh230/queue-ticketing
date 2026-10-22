package com.queue.ticketing.modules.ticket.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.queue.ticketing.modules.ticket.entity.Ticket;
import com.queue.ticketing.modules.ticket.enums.TicketStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 取号单 Mapper
 *
 * @author yh
 * @date 2026-10-22
 */
@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {

    /**
     * 根据队列实例ID和状态查询等待中最老的 ticket
     *
     * @param queueInstanceId 队列实例ID
     * @return 最老的等待 ticket
     */
    @Select("SELECT * FROM ticket WHERE queue_instance_id = #{queueInstanceId} " +
            "AND status = 'WAITING' AND deleted = 0 ORDER BY updated_at ASC LIMIT 1")
    Ticket selectOldestWaiting(@Param("queueInstanceId") Long queueInstanceId);

    /**
     * 统计队列实例中指定状态的 ticket 数量
     *
     * @param queueInstanceId 队列实例ID
     * @param status          状态
     * @return 数量
     */
    default Long countByInstanceAndStatus(Long queueInstanceId, String status) {
        return selectCount(new LambdaQueryWrapper<Ticket>()
                .eq(Ticket::getQueueInstanceId, queueInstanceId)
                .eq(Ticket::getStatus, status));
    }

    /**
     * 查询用户在该队列实例中的进行中的 ticket
     *
     * @param queueInstanceId 队列实例ID
     * @param userId          用户ID
     * @return ticket 列表
     */
    default List<Ticket> selectActiveByUser(Long queueInstanceId, String userId) {
        return selectList(new LambdaQueryWrapper<Ticket>()
                .eq(Ticket::getQueueInstanceId, queueInstanceId)
                .eq(Ticket::getUserId, userId)
                .in(Ticket::getStatus, TicketStatus.WAITING.getCode(), TicketStatus.CALLED.getCode()));
    }

    /**
     * 查询用户全局进行中的 ticket
     *
     * @param userId 用户ID
     * @return ticket 列表
     */
    default List<Ticket> selectActiveByUserId(String userId) {
        return selectList(new LambdaQueryWrapper<Ticket>()
                .eq(Ticket::getUserId, userId)
                .in(Ticket::getStatus, TicketStatus.WAITING.getCode(), TicketStatus.CALLED.getCode()));
    }

    /**
     * 乐观锁更新 ticket 状态
     *
     * @param id          ticket ID
     * @param newStatus   新状态
     * @param oldStatus   旧状态
     * @param counterId   柜台ID
     * @param calledAt    叫号时间
     * @return 影响行数
     */
    @Update("UPDATE ticket SET status = #{newStatus}, counter_id = #{counterId}, " +
            "called_at = #{calledAt}, updated_at = NOW() " +
            "WHERE id = #{id} AND status = #{oldStatus} AND deleted = 0")
    int updateStatusByOldStatus(@Param("id") Long id,
                                @Param("newStatus") String newStatus,
                                @Param("oldStatus") String oldStatus,
                                @Param("counterId") Long counterId,
                                @Param("calledAt") java.time.LocalDateTime calledAt);

    /**
     * 查询队列实例中按更新时间排序的前 N 个等待 ticket
     *
     * @param queueInstanceId 队列实例ID
     * @param limit           限制数量
     * @return 等待 ticket 列表
     */
    @Select("SELECT * FROM ticket WHERE queue_instance_id = #{queueInstanceId} " +
            "AND status = 'WAITING' AND deleted = 0 ORDER BY updated_at ASC LIMIT #{limit}")
    List<Ticket> selectWaitingTicketsOrdered(@Param("queueInstanceId") Long queueInstanceId,
                                             @Param("limit") int limit);

    /**
     * 查询队列实例下最大的号码序号
     *
     * @param queueInstanceId 队列实例ID
     * @param prefix          号码前缀
     * @return 最大序号，无数据时返回 0
     */
    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(ticket_no, LENGTH(#{prefix}) + 1) AS UNSIGNED)), 0) " +
            "FROM ticket WHERE queue_instance_id = #{queueInstanceId} AND deleted = 0")
    Long selectMaxTicketSeq(@Param("queueInstanceId") Long queueInstanceId,
                            @Param("prefix") String prefix);
}
