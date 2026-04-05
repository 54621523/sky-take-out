package com.sky.order.mapper.mapstruct;

import com.sky.order.domain.po.OrderDetail;
import com.sky.order.domain.po.Orders;
import com.sky.order.dto.OrderDetailDTO;
import com.sky.order.dto.OrdersDTO;
import com.sky.order.vo.OrderDetailVO;
import com.sky.order.vo.OrderVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface OrderMapStruct {

    OrderMapStruct INSTANCE = Mappers.getMapper(OrderMapStruct.class);

    // ========== Orders 相关转换 ==========

    /**
     * DTO 转 PO（用于新增/修改订单）
     */
    @Mappings({
            @Mapping(target = "checkoutTime", ignore = true),
            @Mapping(target = "payStatus", ignore = true),
            @Mapping(target = "cancelReason", ignore = true),
            @Mapping(target = "rejectionReason", ignore = true),
            @Mapping(target = "cancelTime", ignore = true),
            @Mapping(target = "estimatedDeliveryTime", ignore = true),
            @Mapping(target = "deliveryTime", ignore = true)
    })
    Orders ordersDto2Po(OrdersDTO dto);

    /**
     * PO 转 VO（用于查询返回）
     */
    @Mappings({
            @Mapping(target = "orderDishes", ignore = true),
            @Mapping(target = "orderDetailList", ignore = true)
    })
    OrderVO ordersPo2Vo(Orders po);

    List<OrderVO> ordersPo2Vo(List<Orders> pos);

    // ========== OrderDetail 相关转换 ==========

    /**
     * DTO 转 PO（用于新增/修改订单明细）
     */
    OrderDetail orderDetailDto2Po(OrderDetailDTO dto);

    List<OrderDetail> orderDetailDto2Po(List<OrderDetailDTO> dtos);

    /**
     * PO 转 VO（用于查询返回）
     */
    OrderDetailVO orderDetailPo2Vo(OrderDetail po);

    List<OrderDetailVO> orderDetailPo2Vo(List<OrderDetail> pos);
}
