package com.sky.order.dubboService;

import com.sky.order.dto.*;
import com.sky.order.vo.*;
import com.sky.result.PageResult;

import java.time.LocalDate;

public interface OrderDubboService {


    PageResult pageQuery4Shop(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderVO getOrderDetailById(Long id);

    void confirm(Long id);

    void reject(OrdersRejectionDTO ordersRejectionDTO);

    void cancel4Shop(OrdersCancelDTO ordersCancelDTO);

    void complete(Long id);

    void delivery(Long id);

    OrderStatisticsVO statistics();


    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    PageResult pageQuery4User(OrdersPageQueryDTO ordersPageQueryDTO);

    void cancel4User(Long id);

    void reminder(Long id);

    void repetition(Long id);

    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    TurnoverReportVO turnoverStatistics(LocalDate now, LocalDate now1);

    OrderOverViewVO getOverViewOrders();

    OrderReportVO ordersStatistics(LocalDate begin, LocalDate end);

    SalesTop10ReportVO top10(LocalDate begin, LocalDate end);
}
