package com.sky.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.coupon.domain.po.UserCoupon;
import com.sky.coupon.vo.UserCouponVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    List<UserCouponVO> selectMyCoupons(@Param("userId") Long userId);
}