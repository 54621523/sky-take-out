package com.sky.order.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.order.domain.po.Orders;
import com.sky.order.dto.*;
import com.sky.order.vo.*;
import com.sky.result.PageResult;

import java.time.LocalDate;

public interface OrderService extends IService<Orders> {


    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    OrderVO getOrderDetailById(Long id);

    PageResult pageQuery4User(OrdersPageQueryDTO ordersPageQueryDTO);

    PageResult pageQuery4Shop(OrdersPageQueryDTO ordersPageQueryDTO);

    void cancel4User(Long id);

    void repetition(Long id);

    void confirm(Long id);

    void reject(OrdersRejectionDTO ordersRejectionDTO);

    void cancel4Shop(OrdersCancelDTO ordersCancelDTO);

    void complete(Long id);

    void delivery(Long id);

    OrderStatisticsVO statistics();

    void reminder(Long id);

    OrderOverViewVO getOverViewOrders();

    SalesTop10ReportVO top10(LocalDate begin, LocalDate end);

    OrderReportVO ordersStatistics(LocalDate begin, LocalDate end);

    TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end);
}
