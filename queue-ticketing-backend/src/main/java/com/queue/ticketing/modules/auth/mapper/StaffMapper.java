package com.queue.ticketing.modules.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.queue.ticketing.modules.auth.entity.Staff;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工账号 Mapper
 *
 * @author yh
 * @date 2026-10-22
 */
@Mapper
public interface StaffMapper extends BaseMapper<Staff> {
}
