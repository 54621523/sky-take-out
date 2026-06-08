package com.sky.product.service;

import com.sky.product.vo.SetmealVO;
import com.sky.result.PageResult;

import java.math.BigDecimal;

public interface SetmealSearchService {

    PageResult searchAndConvert(String name, int page, int pageSize, Long categoryId, Integer status, boolean includeDishes);

    PageResult smartSearch(String keyword, int page, int pageSize, Long categoryId, Integer status,
                           BigDecimal minPrice, BigDecimal maxPrice);


    void deleteSetmealIndex(Long setmealId);
}
