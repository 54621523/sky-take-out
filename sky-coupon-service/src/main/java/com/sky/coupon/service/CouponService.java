package com.sky.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.coupon.domain.po.Coupon;
import com.sky.coupon.dto.CouponPageQueryDTO;
import com.sky.coupon.dto.CouponDTO;
import com.sky.coupon.vo.CouponVO;
import com.sky.coupon.vo.UserCouponVO;
import com.sky.result.PageResult;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService extends IService<Coupon> {

    void saveCoupon(CouponDTO dto);

    PageResult pageQuery(CouponPageQueryDTO dto);

    CouponVO getCouponById(Long id);

    void updateCoupon(CouponDTO dto);

    void startOrStop(Integer status, Long id);

    void claimCoupon(Long couponId, Long userId);

    List<UserCouponVO> getMyCoupons(Long userId);

    List<CouponVO> getAvailableCoupons(Long userId);

    BigDecimal useCoupon(Long userCouponId, Long userId, BigDecimal orderAmount);

    void restoreCoupon(Long userCouponId, Long userId);
}