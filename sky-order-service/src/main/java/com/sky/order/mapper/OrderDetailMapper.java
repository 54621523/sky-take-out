package com.sky.order.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.order.domain.po.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
    void insertBatch(List<OrderDetail> orderDetailList);

    List<OrderDetail> listByOrderId(Long orderId);
}
