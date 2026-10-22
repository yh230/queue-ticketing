package com.queue.ticketing.modules.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.queue.ticketing.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商户实体
 *
 * @author yh
 * @date 2026-10-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant")
public class Merchant extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 商户名称
     */
    private String name;

    /**
     * 行业类型：CATERING-餐饮 MEDICAL-医疗 GOVERNMENT-政务
     */
    private String industryType;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;
}
