-- 取消取号原子脚本
-- KEYS[1] q:{storeId}:users
-- KEYS[2] q:{storeId}:q:{templateId}:meta
-- KEYS[3] q:{storeId}:q:{templateId}:queue
-- KEYS[4] q:{storeId}:q:{templateId}:ticket:{ticketNo}
-- ARGV[1] uid（为空表示店员操作）
-- ARGV[2] nowMillis

local usersKey = KEYS[1]
local metaKey = KEYS[2]
local queueKey = KEYS[3]
local ticketKey = KEYS[4]

local uid = ARGV[1]
local now = ARGV[2]

local ticketUid = redis.call('HGET', ticketKey, 'uid')
if not ticketUid then
    return {0, '', 'TICKET_NOT_FOUND'}
end
if uid ~= '' and ticketUid ~= uid then
    return {0, '', 'FORBIDDEN'}
end

local status = redis.call('HGET', ticketKey, 'status')
if status ~= 'WAITING' and status ~= 'CALLED' then
    return {0, '', 'STATUS_ILLEGAL'}
end

local ticketNo = redis.call('HGET', ticketKey, 'ticketNo')
redis.call('ZREM', queueKey, ticketUid .. ':' .. ticketNo)
redis.call('SREM', usersKey, ticketUid)

if status == 'WAITING' then
    redis.call('HINCRBY', metaKey, 'waitingCount', -1)
else
    redis.call('HSET', metaKey, 'callingTicketNo', '')
end

redis.call('HSET', ticketKey, 'status', 'CANCELLED', 'updatedAt', now)
return {1, ticketUid, 'OK'}
