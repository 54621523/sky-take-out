package com.sky.product.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 菜品分页查询传输对象
 */
@Data
public class DishPageQueryDTO implements Serializable {

    private int page;

    private int pageSize;

    private String name;

    //分类id
    private Integer categoryId;

    //状态 0表示禁用 1表示启用
    private Integer status;

}
