-- 优惠券领取 - 原子操作防超卖
-- KEYS[1] = coupon:stock:{couponId}     库存key
-- KEYS[2] = coupon:claimed:{couponId}:{userId}  用户领取标记key
-- ARGV[1] = perUserLimit  每人限领数量
-- ARGV[2] = expireSeconds  领取标记过期时间（与优惠券有效期对齐）
-- 返回值: 1=成功, 0=已领取达到上限, -1=库存不足

-- 检查用户是否已达到领取上限
local claimed = tonumber(redis.call('GET', KEYS[2]) or '0')
if claimed >= tonumber(ARGV[1]) then
    return 0
end

-- 检查并扣减库存（原子性，防超卖核心）
local stock = tonumber(redis.call('GET', KEYS[1]))
if stock == nil or stock <= 0 then
    return -1
end

local newStock = redis.call('DECR', KEYS[1])
if newStock < 0 then
    -- 并发情况下DECR可能导致负数，回滚
    redis.call('INCR', KEYS[1])
    return -1
end

-- 记录用户领取数量，设置过期时间
redis.call('INCR', KEYS[2])
redis.call('EXPIRE', KEYS[2], tonumber(ARGV[2]))

return 1