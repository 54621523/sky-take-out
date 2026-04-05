package com.sky.goods.query.controller;

import com.sky.product.dubboService.DishDubboService;
import com.sky.product.vo.DishVO;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Api(tags = "用户端菜品相关接口")
@Slf4j
public class DishController {


    @DubboReference
    private DishDubboService dishService;
    /**
     * 根据分类查询菜品
     * @param categoryId
     * @return
     */
    @Cacheable(cacheNames = "dishCache",key = "#categoryId")
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId){
        List<DishVO> list = dishService.listWithFlavors(categoryId);
        return Result.success(list);
    }


}
