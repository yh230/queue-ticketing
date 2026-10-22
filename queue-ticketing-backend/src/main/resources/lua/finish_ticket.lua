-- 完成服务原子脚本
-- KEYS[1] q:{storeId}:q:{templateId}:meta
-- KEYS[2] q:{storeId}:q:{templateId}:ticket:{ticketNo}
-- ARGV[1] uid（为空表示店员操作）
-- ARGV[2] nowMillis

local metaKey = KEYS[1]
local ticketKey = KEYS[2]
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
if status ~= 'CALLED' then
    return {0, '', 'STATUS_ILLEGAL'}
end

redis.call('HSET', ticketKey, 'status', 'SERVED', 'servedAt', now)
redis.call('HSET', metaKey, 'callingTicketNo', '')
return {1, ticketUid, 'OK'}
