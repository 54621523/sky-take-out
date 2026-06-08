package com.sky.coupon.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCoupon implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Integer STATUS_UNUSED = 0;
    public static final Integer STATUS_USED = 1;
    public static final Integer STATUS_EXPIRED = 2;

    @TableId
    private Long id;

    private Long userId;

    private Long couponId;

    private Integer status;

    private Long orderId;

    private LocalDateTime claimTime;

    private LocalDateTime useTime;
}