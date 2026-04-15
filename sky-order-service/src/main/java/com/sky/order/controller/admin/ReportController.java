package com.sky.order.controller.admin;


import com.sky.order.dubboService.OrderDubboService;
import com.sky.order.service.OrderService;
import com.sky.order.vo.OrderReportVO;
import com.sky.order.vo.SalesTop10ReportVO;
import com.sky.order.vo.TurnoverReportVO;
import com.sky.result.Result;
import com.sky.user.dubboService.UserDubboService;
import com.sky.user.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/report")
@Api(tags = "数据统计接口")
public class ReportController {

    @Autowired
    private OrderService orderService;
    @DubboReference
    private UserDubboService userService;


    @ApiOperation("营业额数据统计")
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        TurnoverReportVO turnoverReportVO = orderService.turnoverStatistics(begin, end);
        return Result.success(turnoverReportVO);
    }

    @ApiOperation("新增用户数据统计")
    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        UserReportVO userReportVO = userService.userStatistics(begin, end);
        return Result.success(userReportVO);
    }

    @ApiOperation("订单数据统计")
    @GetMapping("/ordersStatistics")
    public Result<?> ordersStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        OrderReportVO orderReportVO = orderService.ordersStatistics(begin, end);
        return Result.success(orderReportVO);
    }

    @ApiOperation("查询销量排行top10")
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        SalesTop10ReportVO salesTop10ReportVO = orderService.top10(begin, end);
        return Result.success(salesTop10ReportVO);
    }

}
