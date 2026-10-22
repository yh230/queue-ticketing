-- Redis + MQ 改造升级脚本（2026-10-22）
-- 可重复执行

-- 1. ticket 增加 message_id
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'ticket' AND column_name = 'message_id'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE ticket ADD COLUMN message_id VARCHAR(64) DEFAULT NULL COMMENT ''MQ消息ID，用于消费幂等''',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. ticket.message_id 唯一索引
SET @idx_exists = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'ticket' AND index_name = 'uk_message_id'
);
SET @ddl = IF(@idx_exists = 0,
    'ALTER TABLE ticket ADD UNIQUE KEY uk_message_id (message_id)',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 本地消息表
CREATE TABLE IF NOT EXISTS local_message (
    id              BIGINT          NOT NULL PRIMARY KEY COMMENT '消息ID',
    message_id      VARCHAR(64)     NOT NULL COMMENT '业务消息ID',
    topic           VARCHAR(64)     NOT NULL COMMENT '消息主题',
    payload         TEXT            NOT NULL COMMENT '消息内容',
    status          TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0-待发送 1-已发送 2-失败',
    retry_count     INT             NOT NULL DEFAULT 0 COMMENT '重试次数',
    next_retry_at   DATETIME        DEFAULT NULL COMMENT '下次重试时间',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：1-已删除 0-未删除',
    UNIQUE KEY uk_message_id (message_id),
    INDEX idx_status_retry (status, next_retry_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本地消息表';
