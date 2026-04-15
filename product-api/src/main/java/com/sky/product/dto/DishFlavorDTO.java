package com.sky.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 口味传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishFlavorDTO implements Serializable {

    private Long id;

    private Long dishId;

    private String name;

    private String value;

}
