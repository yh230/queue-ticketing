-- 过号原子脚本
-- KEYS[1] q:{storeId}:q:{templateId}:meta
-- KEYS[2] q:{storeId}:q:{templateId}:ticket:{ticketNo}
-- ARGV[1] nowMillis

local metaKey = KEYS[1]
local ticketKey = KEYS[2]
local now = ARGV[1]

local status = redis.call('HGET', ticketKey, 'status')
if not status then
    return {0, '', 'TICKET_NOT_FOUND'}
end
if status ~= 'CALLED' then
    return {0, '', 'STATUS_ILLEGAL'}
end

local uid = redis.call('HGET', ticketKey, 'uid')
redis.call('HSET', ticketKey, 'status', 'MISSED', 'updatedAt', now)
redis.call('HSET', metaKey, 'callingTicketNo', '')
return {1, uid, 'OK'}
