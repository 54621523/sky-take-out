package com.sky.product.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 套餐菜品视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetmealDishVO {

    //份数
    private Integer copies;

    //菜品名称
    private String name;

    //描述信息
    private String description;

    //图片
    private String image;

}
