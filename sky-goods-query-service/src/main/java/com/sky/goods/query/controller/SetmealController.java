package com.sky.goods.query.controller;


import com.sky.product.dubboService.SetmealDubboService;
import com.sky.product.vo.SetmealDishVO;
import com.sky.product.vo.SetmealVO;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "用户套餐相关接口")
public class SetmealController {

    @DubboReference
    private SetmealDubboService setmealService;

    /**
     * 根据分类id查询套餐
     * @param categoryId
     * @return
     */
    @ApiOperation("根据分类id查询套餐")
    @GetMapping("/list")
    @Cacheable(cacheNames = "setmealCache", key = "#categoryId")
    public Result<List<SetmealVO>> listByCategoryId(Long categoryId){
        List<SetmealVO> list = setmealService.list(categoryId);
        return Result.success(list);
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @ApiOperation("根据套餐id查询包含菜品")
    @GetMapping("/dish/{id}")
    public Result<List<SetmealDishVO>> getById(@PathVariable Long id){
        List<SetmealDishVO> list = setmealService.getSetmealDishById(id);
        return Result.success(list);
    }


}
