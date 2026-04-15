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
public class DishDocument implements Serializable {

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
    private List<FlavorDocument> flavors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlavorDocument implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long id;
        private Long dishId;
        private String name;
        private String value;
    }
}
