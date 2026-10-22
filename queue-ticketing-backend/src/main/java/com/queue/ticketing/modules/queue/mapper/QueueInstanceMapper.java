package com.queue.ticketing.modules.queue.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.queue.ticketing.modules.queue.entity.QueueInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

/**
 * 队列实例 Mapper
 *
 * @author yh
 * @date 2026-10-22
 */
@Mapper
public interface QueueInstanceMapper extends BaseMapper<QueueInstance> {

    /**
     * 更新当前叫号与等待人数
     *
     * @param id              队列实例ID
     * @param currentTicketId 当前叫号ticket ID，为null时不更新该字段
     * @param waitCountDelta  等待人数变化量
     * @return 影响行数
     */
    @Update("<script>UPDATE queue_instance SET " +
            "<if test='currentTicketId != null'>current_ticket_id = #{currentTicketId},</if> " +
            "wait_count = wait_count + #{waitCountDelta}, updated_at = NOW() " +
            "WHERE id = #{id} AND deleted = 0</script>")
    int updateCurrentTicket(@Param("id") Long id,
                            @Param("currentTicketId") Long currentTicketId,
                            @Param("waitCountDelta") int waitCountDelta);

    /**
     * 清空当前叫号
     *
     * @param id 队列实例ID
     * @return 影响行数
     */
    @Update("UPDATE queue_instance SET current_ticket_id = NULL, updated_at = NOW() " +
            "WHERE id = #{id} AND deleted = 0")
    int clearCurrentTicket(@Param("id") Long id);

    /**
     * 查询门店某日期队列实例
     *
     * @param storeId    门店ID
     * @param templateId 模板ID
     * @param queueDate  队列日期
     * @return 队列实例
     */
    default QueueInstance selectByStoreAndDate(Long storeId, Long templateId, LocalDate queueDate) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<QueueInstance>()
                .eq(QueueInstance::getStoreId, storeId)
                .eq(QueueInstance::getTemplateId, templateId)
                .eq(QueueInstance::getQueueDate, queueDate));
    }
}
