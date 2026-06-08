package com.sky.coupon.controller;

import com.sky.context.BaseContext;
import com.sky.coupon.service.CouponService;
import com.sky.coupon.vo.CouponVO;
import com.sky.coupon.vo.UserCouponVO;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/coupon")
@Api(tags = "用户优惠券接口")
@Slf4j
public class UserCouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping("/available")
    @ApiOperation("查看可领取的优惠券列表")
    public Result<List<CouponVO>> available() {
        Long userId = BaseContext.getCurrentId();
        List<CouponVO> list = couponService.getAvailableCoupons(userId);
        return Result.success(list);
    }

    @PostMapping("/claim/{couponId}")
    @ApiOperation("领取优惠券")
    public Result claim(@PathVariable Long couponId) {
        Long userId = BaseContext.getCurrentId();
        couponService.claimCoupon(couponId, userId);
        return Result.success();
    }

    @GetMapping("/my")
    @ApiOperation("查看我的优惠券列表")
    public Result<List<UserCouponVO>> myCoupons() {
        Long userId = BaseContext.getCurrentId();
        List<UserCouponVO> list = couponService.getMyCoupons(userId);
        return Result.success(list);
    }
}