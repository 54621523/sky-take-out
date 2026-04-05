package com.sky.admin.operation.controller;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shop")
@Api(tags = "店铺操作接口")
public class ShopController {



    @ApiOperation("设置营业状态")
    @PutMapping("/{status}")
    @CachePut(cacheNames = "shopCache", key = "'SHOP_STATUS'")
    public Result setStatus(@PathVariable Integer status){
        return Result.success(status);
    }


    @ApiOperation("获取营业状态")
    @GetMapping("/status")
    @Cacheable(cacheNames = "shopCache", key = "'SHOP_STATUS'")
    public Result<Integer> getStatus(){
        return Result.success(0);
    }
}
