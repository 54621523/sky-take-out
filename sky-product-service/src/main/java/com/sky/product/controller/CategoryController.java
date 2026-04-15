package com.sky.product.controller;


import com.sky.product.dubboService.CategoryDubboService;
import com.sky.product.service.CategoryService;
import com.sky.product.vo.CategoryVO;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userCategoryController")
@RequestMapping("/user/category")
@Api(tags = "用户端分类接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 按照分类类型查询
     * @param type
     * @return
     */
    @ApiOperation("按分类类型查询")
    @GetMapping("/list")
    public Result<List<CategoryVO>> list(Integer type){
        List<CategoryVO> list = categoryService.list(type);
        return Result.success(list);
    }
}
