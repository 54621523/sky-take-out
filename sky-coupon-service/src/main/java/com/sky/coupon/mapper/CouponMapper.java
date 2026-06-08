package com.sky.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.sky.coupon.domain.po.Coupon;
import com.sky.coupon.dto.CouponPageQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    Page<Coupon> pageQuery(CouponPageQueryDTO dto);

    int decreaseStock(@Param("couponId") Long couponId);
}