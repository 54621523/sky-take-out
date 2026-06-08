package com.sky.coupon.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CouponPageQueryDTO implements Serializable {

    private int page = 1;

    private int pageSize = 10;

    private String name;

    private Integer type;

    private Integer status;
}