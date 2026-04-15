package com.sky.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishSearchSyncMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long dishId;

    private String operation;
}
