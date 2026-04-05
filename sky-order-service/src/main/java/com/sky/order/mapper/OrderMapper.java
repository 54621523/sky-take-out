package com.sky.order.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.sky.order.domain.po.Orders;
import com.sky.order.dto.OrdersPageQueryDTO;
import com.sky.order.vo.OrderOverViewVO;
import com.sky.order.vo.OrderReportVO;
import com.sky.order.vo.SalesTop10ReportVO;
import com.sky.order.vo.TurnoverReportVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {



    Orders getByNumber(String number);

    Orders getById(Long id);

    Page<Orders> page4User(OrdersPageQueryDTO ordersPageQueryDTO);

    Page<Orders> page4Shop(OrdersPageQueryDTO ordersPageQueryDTO);

    Integer countStatus(Integer status);

    void processTimeoutOrder(Integer source, Integer target,LocalDateTime now, LocalDateTime timeout,String cancelReason);

    TurnoverReportVO turnoverStatistics(LocalDate beginTime, LocalDate endTime);

    OrderReportVO ordersStatistics(LocalDate beginTime, LocalDate endTime);

    SalesTop10ReportVO top10(LocalDate beginTime, LocalDate endTime);

    OrderOverViewVO getOverViewOrders();
}
