-- 已有数据库升级脚本（2026-10-22）
-- 新增队列实例 + 状态联合索引，提升容量统计与等待列表查询性能

ALTER TABLE ticket ADD INDEX idx_instance_status (queue_instance_id, status);
