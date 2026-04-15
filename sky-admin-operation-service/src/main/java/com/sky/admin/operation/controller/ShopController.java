package com.sky.admin.operation.controller;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shop")
@Api(tags = "店铺操作接口")
public class ShopController {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @ApiOperation("设置营业状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status){
        stringRedisTemplate.opsForValue().set("SHOP_STATUS", status.toString());
        return Result.success();
    }


    @ApiOperation("获取营业状态")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        stringRedisTemplate.opsForValue().get("SHOP_STATUS");
        return Result.success();
    }
}
