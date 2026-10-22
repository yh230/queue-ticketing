package com.queue.ticketing.common.constant;

/**
 * 排队叫号系统常量
 *
 * @author yh
 * @date 2026-10-22
 */
public final class QueueConstants {

    private QueueConstants() {
    }

    /**
     * 队列号码序列 Redis Key 前缀
     */
    public static final String QUEUE_SEQ_KEY_PREFIX = "queue:seq:";

    /**
     * 叫号分布式锁 Redis Key 前缀
     */
    public static final String CALL_LOCK_KEY_PREFIX = "lock:call:";

    /**
     * 取号分布式锁 Redis Key 前缀
     */
    public static final String TAKE_LOCK_KEY_PREFIX = "lock:take:";

    /**
     * 取号幂等 Redis Key 前缀
     */
    public static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";

    /**
     * 默认号码格式长度（不足补零）
     */
    public static final int TICKET_NO_LENGTH = 3;

    /**
     * 默认前方提醒人数阈值
     */
    public static final int DEFAULT_AHEAD_NOTIFY_COUNT = 3;

    /**
     * 分布式锁默认等待时间（秒）
     */
    public static final long LOCK_WAIT_SECONDS = 3L;

    /**
     * 分布式锁默认持有时间（秒）
     */
    public static final long LOCK_LEASE_SECONDS = 10L;
}
