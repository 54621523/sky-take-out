package com.sky.admin.operation.controller;


import com.sky.admin.operation.service.WorkSpaceService;
import com.sky.admin.vo.BusinessDataVO;
import com.sky.order.dubboService.OrderDubboService;
import com.sky.order.vo.OrderOverViewVO;
import com.sky.product.dubboService.DishDubboService;
import com.sky.product.dubboService.SetmealDubboService;
import com.sky.product.vo.DishOverViewVO;
import com.sky.product.vo.SetmealOverViewVO;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/workspace")
@Api(tags = "工作台接口")
public class WorkSpaceController {


    @Autowired
    private WorkSpaceService workSpaceService;
    @DubboReference
    private OrderDubboService orderService;
    @DubboReference
    private SetmealDubboService setmealService;
    @DubboReference
    private DishDubboService dishService;

    /**
     * 查询今日营业数据
     * @return
     */
    @ApiOperation("查询今日营业数据")
    @GetMapping("/businessData")
    public Result<BusinessDataVO> getBusinessData(){
        BusinessDataVO businessDataVO = workSpaceService.getBusinessData();
        return Result.success(businessDataVO);
    }

    /**
     * 查询套餐总览
     * @return
     */
    @ApiOperation("查询套餐总览")
    @GetMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> getOverViewSetmeals(){
        SetmealOverViewVO setmealOverViewVO = setmealService.getOverViewSetmeals();
        return Result.success(setmealOverViewVO);
    }

    /**
     * 查询菜品总览
     * @return
     */
    @ApiOperation("查询菜品总览")
    @GetMapping("/overviewDishes")
    public Result<DishOverViewVO> getOverViewDishes(){
        DishOverViewVO dishOverViewVO = dishService.getOverViewDishes();
        return Result.success(dishOverViewVO);
    }

    /**
     * 查询订单总览
     * @return
     */
    @ApiOperation("查询订单总览")
    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO> getOverViewOrders(){
        OrderOverViewVO orderOverViewVO = orderService.getOverViewOrders();
        return Result.success(orderOverViewVO);
    }

}
