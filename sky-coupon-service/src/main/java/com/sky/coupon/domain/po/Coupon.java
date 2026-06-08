package com.sky.coupon.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class Coupon implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Integer STATUS_ENABLED = 1;
    public static final Integer STATUS_DISABLED = 0;

    public static final Integer TYPE_FIXED = 1;
    public static final Integer TYPE_RATE = 2;

    @TableId
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