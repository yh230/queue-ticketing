package com.queue.ticketing.modules.queue.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis 排队核心操作
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisQueueService {

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<List> takeTicketScript;
    private final DefaultRedisScript<List> callTicketScript;
    private final DefaultRedisScript<List> cancelTicketScript;
    private final DefaultRedisScript<List> finishTicketScript;
    private final DefaultRedisScript<List> missTicketScript;
    private final DefaultRedisScript<Long> storeRateLimitScript;

    /**
     * 原子取号
     *
     * @param storeId    门店ID
     * @param templateId 队列模板ID
     * @param uid        用户ID
     * @param requestId  幂等请求ID
     * @param config     队列配置
     * @return Lua 结果
     */
    public List<?> take(Long storeId, Long templateId, String uid, String requestId,
                        String userPhone, String source, QueueConfig config) {
        List<String> keys = List.of(
                usersKey(storeId),
                metaKey(storeId, templateId),
                queueKey(storeId, templateId),
                ticketPrefix(storeId, templateId),
                idemKey(storeId, requestId)
        );
        return stringRedisTemplate.execute(takeTicketScript, keys,
                uid,
                String.valueOf(System.currentTimeMillis()),
                String.valueOf(config.getTtlSeconds()),
                String.valueOf(config.getMaxSize()),
                userPhone == null ? "" : userPhone,
                source == null ? "" : source);
    }

    /**
     * 原子叫号
     *
     * @param storeId    门店ID
     * @param templateId 队列模板ID
     * @return Lua 结果
     */
    public List<?> call(Long storeId, Long templateId) {
        List<String> keys = List.of(
                usersKey(storeId),
                metaKey(storeId, templateId),
                queueKey(storeId, templateId),
                ticketPrefix(storeId, templateId)
        );
        return stringRedisTemplate.execute(callTicketScript, keys,
                String.valueOf(System.currentTimeMillis()),
                "86400");
    }

    /**
     * 原子取消取号
     *
     * @param storeId    门店ID
     * @param templateId 队列模板ID
     * @param uid        用户ID
     * @param ticketNo   排队号
     * @return Lua 结果
     */
    public List<?> cancel(Long storeId, Long templateId, String uid, long ticketNo) {
        List<String> keys = List.of(
                usersKey(storeId),
                metaKey(storeId, templateId),
                queueKey(storeId, templateId),
                ticketPrefix(storeId, templateId) + ticketNo
        );
        return stringRedisTemplate.execute(cancelTicketScript, keys,
                uid,
                String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 原子完成服务
     *
     * @param storeId    门店ID
     * @param templateId 队列模板ID
     * @param uid        用户ID，店员操作传空字符串
     * @param ticketNo   排队号
     * @return Lua 结果
     */
    public List<?> finish(Long storeId, Long templateId, String uid, long ticketNo) {
        List<String> keys = List.of(
                metaKey(storeId, templateId),
                ticketPrefix(storeId, templateId) + ticketNo
        );
        return stringRedisTemplate.execute(finishTicketScript, keys,
                uid == null ? "" : uid,
                String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 原子过号
     *
     * @param storeId    门店ID
     * @param templateId 队列模板ID
     * @param ticketNo   排队号
     * @return Lua 结果
     */
    public List<?> miss(Long storeId, Long templateId, long ticketNo) {
        List<String> keys = List.of(
                metaKey(storeId, templateId),
                ticketPrefix(storeId, templateId) + ticketNo
        );
        return stringRedisTemplate.execute(missTicketScript, keys,
                String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 门店令牌桶限流
     *
     * @param storeId 门店ID
     * @param config  队列配置
     * @return 是否放行
     */
    public boolean tryAcquireStoreToken(Long storeId, QueueConfig config) {
        String key = "rate:store:{" + storeId + "}";
        Long result = stringRedisTemplate.execute(storeRateLimitScript,
                List.of(key),
                String.valueOf(config.getRatePerSecond()),
                String.valueOf(config.getRateCapacity()),
                String.valueOf(System.currentTimeMillis()));
        return result != null && result == 1L;
    }

    /**
     * 查询排队进度
     *
     * @param storeId    门店ID
     * @param templateId 队列模板ID
     * @param ticketNo   排队号
     * @return 进度信息
     */
    public Map<String, String> progress(Long storeId, Long templateId, long ticketNo) {
        Map<Object, Object> ticket = stringRedisTemplate.opsForHash()
                .entries(ticketPrefix(storeId, templateId) + ticketNo);
        if (ticket.isEmpty()) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        ticket.forEach((k, v) -> result.put(String.valueOf(k), String.valueOf(v)));

        String uid = result.get("uid");
        Long rank = stringRedisTemplate.opsForZSet()
                .rank(queueKey(storeId, templateId), uid + ":" + ticketNo);
        result.put("aheadCount", String.valueOf(rank == null ? 0 : rank));
        return result;
    }

    /**
     * 查询取号单详情
     *
     * @param storeId    门店ID
     * @param templateId 队列模板ID
     * @param ticketNo   排队号
     * @return 详情
     */
    public Map<Object, Object> getTicketDetail(Long storeId, Long templateId, long ticketNo) {
        return stringRedisTemplate.opsForHash()
                .entries(ticketPrefix(storeId, templateId) + ticketNo);
    }

    /**
     * 查询用户当前取号单
     *
     * @param uid     用户ID
     * @param storeId 门店ID
     * @return 排队号，无则返回 null
     */
    public String getActiveTicketNo(String uid, Long storeId) {
        Object value = stringRedisTemplate.opsForHash().get(activeKey(uid), String.valueOf(storeId));
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 记录用户当前取号单
     *
     * @param uid      用户ID
     * @param storeId  门店ID
     * @param ticketNo 排队号
     */
    public void setActiveTicketNo(String uid, Long storeId, long ticketNo) {
        String key = activeKey(uid);
        stringRedisTemplate.opsForHash().put(key, String.valueOf(storeId), String.valueOf(ticketNo));
        stringRedisTemplate.expire(key, java.time.Duration.ofHours(24));
    }

    /**
     * 清除用户当前取号单
     *
     * @param uid     用户ID
     * @param storeId 门店ID
     */
    public void clearActiveTicketNo(String uid, Long storeId) {
        stringRedisTemplate.opsForHash().delete(activeKey(uid), String.valueOf(storeId));
    }

    /**
     * 查询队列元数据
     *
     * @param storeId    门店ID
     * @param templateId 队列模板ID
     * @return 元数据
     */
    public Map<Object, Object> getMeta(Long storeId, Long templateId) {
        return stringRedisTemplate.opsForHash().entries(metaKey(storeId, templateId));
    }

    /**
     * 查询队列前 N 个等待号码
     *
     * @param storeId    门店ID
     * @param templateId 队列模板ID
     * @param limit      数量
     * @return 等待成员列表
     */
    public List<String> getWaitingMembers(Long storeId, Long templateId, int limit) {
        Set<String> members = stringRedisTemplate.opsForZSet()
                .range(queueKey(storeId, templateId), 0, limit - 1L);
        return members == null ? new ArrayList<>() : new ArrayList<>(members);
    }

    private String tag(Long storeId) {
        return "{q" + storeId + "}";
    }

    private String usersKey(Long storeId) {
        return tag(storeId) + ":users";
    }

    private String metaKey(Long storeId, Long templateId) {
        return tag(storeId) + ":q:" + templateId + ":meta";
    }

    private String queueKey(Long storeId, Long templateId) {
        return tag(storeId) + ":q:" + templateId + ":queue";
    }

    private String ticketPrefix(Long storeId, Long templateId) {
        return tag(storeId) + ":q:" + templateId + ":ticket:";
    }

    private String idemKey(Long storeId, String requestId) {
        return tag(storeId) + ":idem:" + requestId;
    }

    private String activeKey(String uid) {
        return "q:user:" + uid + ":active";
    }
}
