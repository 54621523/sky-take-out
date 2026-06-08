package com.sky.coupon.controller;

import com.sky.coupon.dto.CouponPageQueryDTO;
import com.sky.coupon.dto.CouponDTO;
import com.sky.coupon.service.CouponService;
import com.sky.coupon.vo.CouponVO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/coupon")
@Api(tags = "后台优惠券管理接口")
@Slf4j
public class AdminCouponController {

    @Autowired
    private CouponService couponService;

    @PostMapping
    @ApiOperation("新增优惠券")
    public Result save(@RequestBody CouponDTO dto) {
        couponService.saveCoupon(dto);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("优惠券分页查询")
    public Result<PageResult> page(CouponPageQueryDTO dto) {
        PageResult pageResult = couponService.pageQuery(dto);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询优惠券")
    public Result<CouponVO> getById(@PathVariable Long id) {
        CouponVO vo = couponService.getCouponById(id);
        return Result.success(vo);
    }

    @PutMapping
    @ApiOperation("修改优惠券")
    public Result update(@RequestBody CouponDTO dto) {
        couponService.updateCoupon(dto);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("优惠券启用、停用")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        couponService.startOrStop(status, id);
        return Result.success();
    }
}