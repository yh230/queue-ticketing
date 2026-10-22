-- 门店维度令牌桶限流
-- KEYS[1] rate:store:{storeId}
-- ARGV[1] ratePerSecond
-- ARGV[2] capacity
-- ARGV[3] nowMillis

local key = KEYS[1]
local rate = tonumber(ARGV[1])
local capacity = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

local data = redis.call('HMGET', key, 'tokens', 'last')
local tokens = tonumber(data[1])
local last = tonumber(data[2])

if tokens == nil then
    tokens = capacity
    last = now
end

local delta = math.max(0, now - last)
tokens = math.min(capacity, tokens + delta * rate / 1000)
last = now

if tokens < 1 then
    redis.call('HMSET', key, 'tokens', tokens, 'last', last)
    redis.call('EXPIRE', key, 2)
    return 0
end

tokens = tokens - 1
redis.call('HMSET', key, 'tokens', tokens, 'last', last)
redis.call('EXPIRE', key, 2)
return 1
