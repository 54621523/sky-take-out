package com.sky.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.coupon.domain.po.Coupon;
import com.sky.coupon.domain.po.UserCoupon;
import com.sky.coupon.dto.CouponPageQueryDTO;
import com.sky.coupon.dto.CouponDTO;
import com.sky.coupon.dubboService.CouponDubboService;
import com.sky.coupon.mapper.CouponMapper;
import com.sky.coupon.mapper.UserCouponMapper;
import com.sky.coupon.service.CouponService;
import com.sky.coupon.vo.CouponVO;
import com.sky.coupon.vo.UserCouponVO;
import com.sky.exception.CouponBusinessException;
import com.sky.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@DubboService(interfaceClass = CouponDubboService.class)
@Service
@Slf4j
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService, CouponDubboService {

    private static final String COUPON_STOCK_KEY = "coupon:stock:";
    private static final String COUPON_CLAIMED_KEY = "coupon:claimed:";

    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private UserCouponMapper userCouponMapper;
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    private DefaultRedisScript<Long> claimCouponScript;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCoupon(CouponDTO dto) {
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(dto, coupon);
        coupon.setAvailableStock(dto.getTotalStock());
        coupon.setStatus(Coupon.STATUS_ENABLED);
        coupon.setCreateTime(LocalDateTime.now());
        coupon.setUpdateTime(LocalDateTime.now());
        couponMapper.insert(coupon);
        redisTemplate.opsForValue().set(
                COUPON_STOCK_KEY + coupon.getId(),
                coupon.getAvailableStock()
        );
    }

    @Override
    public PageResult pageQuery(CouponPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Coupon> page = couponMapper.pageQuery(dto);
        List<CouponVO> voList = page.getResult().stream()
                .map(this::toVO)
                .toList();
        return new PageResult(page.getTotal(), voList);
    }



    @Override
    public CouponVO getCouponById(Long id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new CouponBusinessException("优惠券不存在");
        }
        return toVO(coupon);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCoupon(CouponDTO dto) {
        Coupon existing = couponMapper.selectById(dto.getId());
        if (existing == null) {
            throw new CouponBusinessException("优惠券不存在");
        }
        Coupon template = new Coupon();
        BeanUtils.copyProperties(dto, template);
        template.setUpdateTime(LocalDateTime.now());
        if (dto.getTotalStock() != null) {
            int claimed = existing.getTotalStock() - existing.getAvailableStock();
            template.setAvailableStock(dto.getTotalStock() - claimed);
        }
        couponMapper.updateById(template);
        if (dto.getTotalStock() != null) {
            redisTemplate.opsForValue().set(
                    COUPON_STOCK_KEY + dto.getId(),
                    template.getAvailableStock()
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startOrStop(Integer status, Long id) {
        couponMapper.update(new LambdaUpdateWrapper<Coupon>()
                .eq(Coupon::getId, id)
                .set(Coupon::getStatus, status)
                .set(Coupon::getUpdateTime, LocalDateTime.now()));
        if (Coupon.STATUS_DISABLED.equals(status)) {
            redisTemplate.delete(COUPON_STOCK_KEY + id);
        } else {
            Coupon template = couponMapper.selectById(id);
            if (template != null) {
                redisTemplate.opsForValue().set(
                        COUPON_STOCK_KEY + id,
                        template.getAvailableStock()
                );
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimCoupon(Long couponId, Long userId) {
        Coupon template = couponMapper.selectById(couponId);
        if (template == null || !Coupon.STATUS_ENABLED.equals(template.getStatus())) {
            throw new CouponBusinessException("优惠券不存在或已停用");
        }
        if (LocalDateTime.now().isAfter(template.getEndTime())) {
            throw new CouponBusinessException("优惠券已过期");
        }

        String stockKey = COUPON_STOCK_KEY + couponId;
        String claimedKey = COUPON_CLAIMED_KEY + couponId + ":" + userId;

        if (Boolean.FALSE.equals(redisTemplate.hasKey(stockKey))) {
            redisTemplate.opsForValue().set(stockKey, template.getAvailableStock());
        }

        long expireSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), template.getEndTime());
        if (expireSeconds <= 0) {
            expireSeconds = 86400;
        }

        Long result = redisTemplate.execute(
                claimCouponScript,
                Arrays.asList(stockKey, claimedKey),
                template.getPerUserLimit(),
                expireSeconds
        );

        if (result == null) {
            throw new CouponBusinessException("优惠券领取失败，请重试");
        }
        if (result == 0) {
            throw new CouponBusinessException("已达到该优惠券领取上限");
        }
        if (result == -1) {
            throw new CouponBusinessException("优惠券已被领完");
        }

        int rows = couponMapper.decreaseStock(couponId);
        if (rows == 0) {
            redisTemplate.opsForValue().increment(stockKey);
            redisTemplate.delete(claimedKey);
            throw new CouponBusinessException("优惠券已被领完");
        }

        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .status(UserCoupon.STATUS_UNUSED)
                .claimTime(LocalDateTime.now())
                .build();
        userCouponMapper.insert(userCoupon);
    }

    @Override
    public List<UserCouponVO> getMyCoupons(Long userId) {
        return userCouponMapper.selectMyCoupons(userId);
    }

    @Override
    public List<CouponVO> getAvailableCoupons(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> templates = couponMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Coupon>()
                        .eq(Coupon::getStatus, Coupon.STATUS_ENABLED)
                        .le(Coupon::getStartTime, now)
                        .ge(Coupon::getEndTime, now)
                        .gt(Coupon::getAvailableStock, 0));
        return templates.stream().map(this::toVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal useCoupon(Long userCouponId, Long userId, BigDecimal orderAmount) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null || !userCoupon.getUserId().equals(userId)) {
            throw new CouponBusinessException("优惠券不存在");
        }
        if (!UserCoupon.STATUS_UNUSED.equals(userCoupon.getStatus())) {
            throw new CouponBusinessException("优惠券不可用");
        }

        Coupon template = couponMapper.selectById(userCoupon.getCouponId());
        if (template == null || LocalDateTime.now().isAfter(template.getEndTime())) {
            throw new CouponBusinessException("优惠券已过期");
        }
        if (orderAmount.compareTo(template.getMinOrderAmount()) < 0) {
            throw new CouponBusinessException("订单金额未达到优惠券最低消费");
        }

        BigDecimal discount = calculateDiscount(template, orderAmount);

        userCouponMapper.update(new LambdaUpdateWrapper<UserCoupon>()
                .eq(UserCoupon::getId, userCouponId)
                .set(UserCoupon::getStatus, UserCoupon.STATUS_USED)
                .set(UserCoupon::getUseTime, LocalDateTime.now()));

        return discount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreCoupon(Long userCouponId, Long userId) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null || !userCoupon.getUserId().equals(userId)) {
            return;
        }
        if (!UserCoupon.STATUS_USED.equals(userCoupon.getStatus())) {
            return;
        }

        userCouponMapper.update(new LambdaUpdateWrapper<UserCoupon>()
                .eq(UserCoupon::getId, userCouponId)
                .set(UserCoupon::getStatus, UserCoupon.STATUS_UNUSED)
                .set(UserCoupon::getUseTime, null));

        couponMapper.update(new LambdaUpdateWrapper<Coupon>()
                .eq(Coupon::getId, userCoupon.getCouponId())
                .setSql("available_stock = available_stock + 1"));

        String stockKey = COUPON_STOCK_KEY + userCoupon.getCouponId();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(stockKey))) {
            redisTemplate.opsForValue().increment(stockKey);
        }

        String claimedKey = COUPON_CLAIMED_KEY + userCoupon.getCouponId() + ":" + userId;
        redisTemplate.delete(claimedKey);
    }

    private BigDecimal calculateDiscount(Coupon template, BigDecimal orderAmount) {
        if (Coupon.TYPE_FIXED.equals(template.getType())) {
            BigDecimal discount = template.getDiscountAmount();
            if (discount.compareTo(orderAmount) > 0) {
                discount = orderAmount;
            }
            return discount;
        } else if (Coupon.TYPE_RATE.equals(template.getType())) {
            BigDecimal rate = template.getDiscountRate();
            BigDecimal discount = orderAmount.multiply(BigDecimal.ONE.subtract(rate))
                    .setScale(2, RoundingMode.HALF_UP);
            if (discount.compareTo(orderAmount) > 0) {
                discount = orderAmount;
            }
            return discount;
        }
        return BigDecimal.ZERO;
    }

    private CouponVO toVO(Coupon template) {
        CouponVO vo = new CouponVO();
        BeanUtils.copyProperties(template, vo);
        return vo;
    }
}