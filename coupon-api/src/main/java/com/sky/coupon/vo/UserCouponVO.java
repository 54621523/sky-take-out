package com.sky.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponVO implements Serializable {

    private Long id;

    private Long couponId;

    private String couponName;

    private Integer couponType;

    private BigDecimal discountAmount;

    private BigDecimal discountRate;

    private BigDecimal minOrderAmount;

    private Integer status;

    private LocalDateTime claimTime;

    private LocalDateTime useTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}