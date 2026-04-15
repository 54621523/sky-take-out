package com.sky.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 套餐菜品传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetmealDishDTO implements Serializable {


    private Long id;

    private Long setmealId;

    private Long dishId;

    private String name;

    private BigDecimal price;

    private Integer copies;

}
