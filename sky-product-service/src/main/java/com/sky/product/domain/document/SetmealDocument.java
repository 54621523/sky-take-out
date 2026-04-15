package com.sky.product.domain.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetmealDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private BigDecimal price;
    private String image;
    private String description;
    private Integer status;
    private LocalDateTime updateTime;
    private List<SetmealDishDocument> setmealDishes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SetmealDishDocument implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long id;
        private Long setmealId;
        private Long dishId;
        private String name;
        private BigDecimal price;
        private Integer copies;
    }
}
