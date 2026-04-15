package com.sky.order.dto;

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
public class OrderMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;

    private String orderNumber;

    private Integer status;

    private Long userId;

    private LocalDateTime orderTime;

    private LocalDateTime operateTime;

    private String content;
}
