package com.queue.ticketing.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果包装
 *
 * @param <T> 数据类型
 * @author yh
 * @date 2026-10-22
 */
@Data
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 数据列表
     */
    private List<T> records;

    public PageResult() {
    }

    public PageResult(Long current, Long size, Long total, Long pages, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.pages = pages;
        this.records = records;
    }

    /**
     * 从 MyBatis-Plus 分页对象转换
     *
     * @param page MyBatis-Plus 分页对象
     * @return 分页结果
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        if (page == null) {
            return empty(1L, 10L);
        }
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getPages(), page.getRecords());
    }

    /**
     * 空分页
     *
     * @param current 当前页
     * @param size    每页大小
     * @return 空分页结果
     */
    public static <T> PageResult<T> empty(Long current, Long size) {
        return new PageResult<>(current, size, 0L, 0L, Collections.emptyList());
    }
}
