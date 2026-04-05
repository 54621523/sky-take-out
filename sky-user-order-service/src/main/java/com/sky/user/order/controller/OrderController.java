package com.sky.user.order.controller;

import com.sky.context.BaseContext;
import com.sky.order.dto.OrdersPageQueryDTO;
import com.sky.order.dto.OrdersPaymentDTO;
import com.sky.order.dto.OrdersSubmitDTO;
import com.sky.order.dubboService.OrderDubboService;
import com.sky.order.vo.OrderPaymentVO;
import com.sky.order.vo.OrderSubmitVO;
import com.sky.order.vo.OrderVO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.web.bind.annotation.*;

@RestController("UserOrderController")
@RequestMapping("/user/order")
@Api(tags = "订单接口")
@Slf4j
public class OrderController {

    @DubboReference
    private OrderDubboService orderService;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);

    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }


    @GetMapping("/orderDetail/{id}")
    @ApiOperation("获取订单详细信息")
    public Result<OrderVO> getOrderDetailById(@PathVariable Long id){
        OrderVO orderVO = orderService.getOrderDetailById(id);
        return Result.success(orderVO);
    }


    @GetMapping("/historyOrders")
    @ApiOperation("订单分页查询")
    public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO){
        PageResult page = orderService.pageQuery4User(ordersPageQueryDTO);
        return Result.success(page);

    }

    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long id){
        orderService.cancel4User(id);
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long id){
        orderService.repetition(id);
        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    @ApiOperation("催单")
    public Result reminder(@PathVariable Long id){
        orderService.reminder(id);
        return Result.success();
    }
}
