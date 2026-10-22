-- 已有数据库升级脚本（2026-10-22）
-- 新增队列实例 + 状态联合索引，提升容量统计与等待列表查询性能
-- 脚本可重复执行

SET @index_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'ticket'
      AND index_name = 'idx_instance_status'
);

SET @ddl = IF(@index_exists = 0,
    'ALTER TABLE ticket ADD INDEX idx_instance_status (queue_instance_id, status)',
    'SELECT ''idx_instance_status already exists'' AS message');

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
