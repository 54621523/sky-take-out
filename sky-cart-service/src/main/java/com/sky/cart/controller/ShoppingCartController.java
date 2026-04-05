package com.sky.cart.controller;


import com.sky.cart.dto.ShoppingCartDTO;
import com.sky.cart.service.ShoppingCartService;
import com.sky.cart.vo.ShoppingCartVO;
import com.sky.context.BaseContext;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags = "购物车接口")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 获取购物车列表
     * @return
     */
    @ApiOperation("获取购物车列表")
    @GetMapping("/list")
    public Result<List<ShoppingCartVO>> list(){

        Long userId = BaseContext.getCurrentId();
        List<ShoppingCartVO> list = shoppingCartService.listByUserId(userId);
        return Result.success(list);
    }

    @ApiOperation("添加购物车")
    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        shoppingCartService.save(shoppingCartDTO);
        return Result.success();
    }

    @PostMapping("/sub")
    @ApiOperation("删除购物车")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO){
        shoppingCartService.delete(shoppingCartDTO);
        return Result.success();
    }

    @ApiOperation("清空购物车")
    @DeleteMapping("/clean")
    public Result clean(){
        shoppingCartService.clean();
        return Result.success();
    }
}
