CREATE DATABASE IF NOT EXISTS queue_ticketing_db
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE queue_ticketing_db;

-- 商户
CREATE TABLE IF NOT EXISTS merchant (
    id              BIGINT          NOT NULL PRIMARY KEY COMMENT '商户ID',
    name            VARCHAR(64)     NOT NULL COMMENT '商户名称',
    industry_type   VARCHAR(32)     NOT NULL COMMENT '行业类型：CATERING/MEDICAL/GOVERNMENT',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：1-已删除 0-未删除',
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户表';

-- 门店
CREATE TABLE IF NOT EXISTS store (
    id              BIGINT          NOT NULL PRIMARY KEY COMMENT '门店ID',
    merchant_id     BIGINT          NOT NULL COMMENT '商户ID',
    name            VARCHAR(64)     NOT NULL COMMENT '门店名称',
    address         VARCHAR(256)    DEFAULT NULL COMMENT '门店地址',
    business_hours  VARCHAR(64)     DEFAULT NULL COMMENT '营业时间，如 09:00-22:00',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：1-已删除 0-未删除',
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店表';

-- 柜台/窗口
CREATE TABLE IF NOT EXISTS counter (
    id                  BIGINT          NOT NULL PRIMARY KEY COMMENT '柜台ID',
    store_id            BIGINT          NOT NULL COMMENT '门店ID',
    name                VARCHAR(64)     NOT NULL COMMENT '柜台名称',
    status              TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：1-已删除 0-未删除',
    INDEX idx_store_id (store_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='柜台表';

-- 柜台与队列模板关联
CREATE TABLE IF NOT EXISTS counter_queue_template (
    id                  BIGINT          NOT NULL PRIMARY KEY COMMENT '关联ID',
    counter_id          BIGINT          NOT NULL COMMENT '柜台ID',
    queue_template_id   BIGINT          NOT NULL COMMENT '队列模板ID',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：1-已删除 0-未删除',
    UNIQUE KEY uk_counter_template (counter_id, queue_template_id),
    INDEX idx_counter_id (counter_id),
    INDEX idx_template_id (queue_template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='柜台与队列模板关联表';

-- 队列模板
CREATE TABLE IF NOT EXISTS queue_template (
    id              BIGINT          NOT NULL PRIMARY KEY COMMENT '模板ID',
    merchant_id     BIGINT          NOT NULL COMMENT '商户ID',
    name            VARCHAR(64)     NOT NULL COMMENT '队列名称，如小桌/中桌/大桌',
    prefix          VARCHAR(8)      NOT NULL COMMENT '号码前缀，如 A/B/C',
    capacity        INT             NOT NULL DEFAULT 200 COMMENT '最大容量',
    rules_json      JSON            DEFAULT NULL COMMENT '队列规则JSON：过号规则、优先级、提醒阈值等',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：1-已删除 0-未删除',
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队列模板表';

-- 队列实例
CREATE TABLE IF NOT EXISTS queue_instance (
    id                  BIGINT          NOT NULL PRIMARY KEY COMMENT '实例ID',
    store_id            BIGINT          NOT NULL COMMENT '门店ID',
    template_id         BIGINT          NOT NULL COMMENT '队列模板ID',
    queue_date          DATE            NOT NULL COMMENT '队列日期',
    status              VARCHAR(16)     NOT NULL DEFAULT 'OPEN' COMMENT '状态：OPEN/PAUSED/CLOSED',
    current_ticket_id   BIGINT          DEFAULT NULL COMMENT '当前叫号 ticket ID',
    wait_count          INT             NOT NULL DEFAULT 0 COMMENT '当前等待人数',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：1-已删除 0-未删除',
    UNIQUE KEY uk_store_template_date (store_id, template_id, queue_date, deleted),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队列实例表';

-- 取号单
CREATE TABLE IF NOT EXISTS ticket (
    id                  BIGINT          NOT NULL PRIMARY KEY COMMENT '取号单ID',
    store_id            BIGINT          NOT NULL COMMENT '门店ID（冗余）',
    queue_instance_id   BIGINT          NOT NULL COMMENT '队列实例ID',
    ticket_no           VARCHAR(16)     NOT NULL COMMENT '排队号码，如 A001',
    user_id             VARCHAR(64)     DEFAULT NULL COMMENT 'C端用户ID（微信openid）',
    user_phone          VARCHAR(16)     DEFAULT NULL COMMENT '用户手机号',
    status              VARCHAR(16)     NOT NULL DEFAULT 'WAITING' COMMENT '状态：WAITING/CALLED/SERVED/MISSED/CANCELLED/EXPIRED',
    source              VARCHAR(16)     NOT NULL DEFAULT 'MINI_PROGRAM' COMMENT '来源：MINI_PROGRAM/STAFF',
    counter_id          BIGINT          DEFAULT NULL COMMENT '服务柜台ID',
    called_at           DATETIME        DEFAULT NULL COMMENT '叫号时间',
    served_at           DATETIME        DEFAULT NULL COMMENT '服务完成时间',
    missed_count        INT             NOT NULL DEFAULT 0 COMMENT '过号次数',
    idempotency_key     VARCHAR(64)     DEFAULT NULL COMMENT '幂等键',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：1-已删除 0-未删除',
    UNIQUE KEY uk_queue_ticket_no (queue_instance_id, ticket_no),
    INDEX idx_store_id (store_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_idempotency_key (idempotency_key),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='取号单表';

-- 通知订阅
CREATE TABLE IF NOT EXISTS subscription (
    id              BIGINT          NOT NULL PRIMARY KEY COMMENT '订阅ID',
    ticket_id       BIGINT          NOT NULL COMMENT '取号单ID',
    user_id         VARCHAR(64)     NOT NULL COMMENT '用户ID',
    notify_type     VARCHAR(32)     NOT NULL COMMENT '通知类型：AHEAD/CALLED/MISSED',
    threshold       INT             DEFAULT NULL COMMENT '前方剩余人数阈值',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：1-已删除 0-未删除',
    INDEX idx_ticket_id (ticket_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知订阅表';

-- 员工账号
CREATE TABLE IF NOT EXISTS staff (
    id              BIGINT          NOT NULL PRIMARY KEY COMMENT '员工ID',
    store_id        BIGINT          NOT NULL COMMENT '所属门店ID',
    username        VARCHAR(64)     NOT NULL COMMENT '登录账号',
    password        VARCHAR(128)    NOT NULL COMMENT '登录密码（加密后）',
    role            VARCHAR(32)     NOT NULL DEFAULT 'STAFF' COMMENT '角色：ADMIN/STORE_MANAGER/STAFF',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：1-已删除 0-未删除',
    UNIQUE KEY uk_username (username),
    INDEX idx_store_id (store_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工账号表';

-- 默认员工账号（密码：123456，MD5加密后）
INSERT IGNORE INTO staff (id, store_id, username, password, role, status) VALUES
(1, 1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', 'ADMIN', 1);

-- 通知日志
CREATE TABLE IF NOT EXISTS notify_log (
    id              BIGINT          NOT NULL PRIMARY KEY COMMENT '日志ID',
    ticket_id       BIGINT          NOT NULL COMMENT '取号单ID',
    channel         VARCHAR(32)     NOT NULL COMMENT '通知渠道：WECHAT_MINI/SMS',
    template_code   VARCHAR(64)     DEFAULT NULL COMMENT '模板编码',
    status          TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0-待发送 1-成功 2-失败',
    error_msg       VARCHAR(512)    DEFAULT NULL COMMENT '失败原因',
    sent_at         DATETIME        DEFAULT NULL COMMENT '发送时间',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_ticket_id (ticket_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知日志表';

-- ============================================================
-- 演示数据
-- ============================================================

-- 演示商户
INSERT IGNORE INTO merchant (id, name, industry_type, status) VALUES
(1, '示例餐饮集团', 'CATERING', 1);

-- 演示门店
INSERT IGNORE INTO store (id, merchant_id, name, address, business_hours, status) VALUES
(1, 1, '示例餐厅·人民广场店', '上海市黄浦区人民大道 100 号', '00:00-23:59', 1);

-- 演示柜台（餐饮场景可理解为叫号台）
INSERT IGNORE INTO counter (id, store_id, name, status) VALUES
(1, 1, '1号叫号台', 1),
(2, 1, '2号叫号台', 1);

-- 演示队列模板
INSERT IGNORE INTO queue_template (id, merchant_id, name, prefix, capacity, rules_json, status) VALUES
(1, 1, '小桌（1-2人）', 'A', 100, '{"capacity":100,"missedDeferCount":1,"maxMissedCount":2,"aheadNotifyThreshold":3,"allowCancel":true,"expireMinutes":120}', 1),
(2, 1, '大桌（3-6人）', 'B', 50, '{"capacity":50,"missedDeferCount":1,"maxMissedCount":2,"aheadNotifyThreshold":3,"allowCancel":true,"expireMinutes":120}', 1);

-- 柜台可服务队列
INSERT IGNORE INTO counter_queue_template (id, counter_id, queue_template_id) VALUES
(1, 1, 1),
(2, 1, 2),
(3, 2, 1),
(4, 2, 2);

-- 今日队列实例（固定 ID 便于演示）
INSERT IGNORE INTO queue_instance (id, store_id, template_id, queue_date, status, wait_count) VALUES
(1, 1, 1, CURDATE(), 'OPEN', 0),
(2, 1, 2, CURDATE(), 'OPEN', 0);
