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
public class CouponVO implements Serializable {

    private Long id;

    private String name;

    private Integer type;

    private BigDecimal discountAmount;

    private BigDecimal discountRate;

    private BigDecimal minOrderAmount;

    private Integer totalStock;

    private Integer availableStock;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer perUserLimit;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}