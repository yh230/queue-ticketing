-- 取号原子脚本
-- KEYS[1] 门店排队用户集合 q:{storeId}:users
-- KEYS[2] 队列元数据 q:{storeId}:q:{templateId}:meta
-- KEYS[3] 队列 q:{storeId}:q:{templateId}:queue
-- KEYS[4] 取号单 Hash 前缀 q:{storeId}:q:{templateId}:ticket:
-- KEYS[5] 幂等 key q:{storeId}:idem:{requestId}
-- ARGV[1] uid
-- ARGV[2] nowMillis
-- ARGV[3] maxSize
-- ARGV[4] userPhone
-- ARGV[5] source

local usersKey = KEYS[1]
local metaKey = KEYS[2]
local queueKey = KEYS[3]
local ticketPfx = KEYS[4]
local idemKey = KEYS[5]

local uid = ARGV[1]
local now = tonumber(ARGV[2])
local maxSize = tonumber(ARGV[3])
local userPhone = ARGV[4]
local source = ARGV[5]

-- 幂等：同一 requestId 重复请求直接返回首次号码
local cached = redis.call('GET', idemKey)
if cached then
    return {1, tonumber(cached), -1, 'IDEMPOTENT'}
end

-- 门店/队列状态
local status = redis.call('HGET', metaKey, 'status')
if status and status ~= 'OPEN' then
    return {0, 0, 0, 'QUEUE_CLOSED'}
end

-- 容量
local waiting = tonumber(redis.call('HGET', metaKey, 'waitingCount') or '0')
if waiting >= maxSize then
    return {0, 0, waiting, 'QUEUE_FULL'}
end

-- 同一门店防重复取号
if redis.call('SISMEMBER', usersKey, uid) == 1 then
    return {0, 0, waiting, 'DUPLICATE'}
end

-- 分配号码
local seq = redis.call('HINCRBY', metaKey, 'currentSeq', 1)

-- 写排队数据
redis.call('SADD', usersKey, uid)
redis.call('ZADD', queueKey, seq, uid .. ':' .. seq)
redis.call('HINCRBY', metaKey, 'waitingCount', 1)
redis.call('HSET', metaKey, 'status', 'OPEN', 'updatedAt', now)

local ticketKey = ticketPfx .. seq
redis.call('HSET', ticketKey,
    'ticketNo', seq,
    'uid', uid,
    'status', 'WAITING',
    'createdAt', now,
    'userPhone', userPhone,
    'source', source)

redis.call('SET', idemKey, seq, 'EX', 300)

return {1, seq, waiting, 'OK'}
