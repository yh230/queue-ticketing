-- 叫号原子脚本：取队首、释放名额、标记当前叫号
-- KEYS[1] q:{storeId}:users
-- KEYS[2] q:{storeId}:q:{templateId}:meta
-- KEYS[3] q:{storeId}:q:{templateId}:queue
-- KEYS[4] q:{storeId}:q:{templateId}:ticket:
-- ARGV[1] nowMillis
-- ARGV[2] ttlSeconds

local usersKey = KEYS[1]
local metaKey = KEYS[2]
local queueKey = KEYS[3]
local ticketPfx = KEYS[4]

local now = tonumber(ARGV[1])
local ttl = tonumber(ARGV[2])

-- 当前叫号未处理，不允许叫下一位
local current = redis.call('HGET', metaKey, 'callingTicketNo')
if current and current ~= '' then
    local currentStatus = redis.call('HGET', ticketPfx .. current, 'status')
    if currentStatus == 'CALLED' then
        return {0, 0, 0, 'CURRENT_NOT_FINISHED'}
    end
end

local first = redis.call('ZRANGE', queueKey, 0, 0, 'WITHSCORES')
if #first == 0 then
    return {0, 0, 0, 'QUEUE_EMPTY'}
end

local member = first[1]
local sep = string.find(member, ':')
local uid = string.sub(member, 1, sep - 1)
local ticketNo = string.sub(member, sep + 1)

redis.call('ZREM', queueKey, member)
redis.call('SREM', usersKey, uid)
redis.call('HINCRBY', metaKey, 'waitingCount', -1)
redis.call('HSET', metaKey, 'callingTicketNo', ticketNo, 'updatedAt', now)

local ticketKey = ticketPfx .. ticketNo
redis.call('HSET', ticketKey, 'status', 'CALLED', 'calledAt', now)
redis.call('EXPIRE', ticketKey, ttl)

local waiting = tonumber(redis.call('HGET', metaKey, 'waitingCount') or '0')
return {1, tonumber(ticketNo), waiting, uid}
