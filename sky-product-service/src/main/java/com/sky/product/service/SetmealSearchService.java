package com.sky.product.service;

import com.sky.product.vo.SetmealVO;
import com.sky.result.PageResult;

public interface SetmealSearchService {

    PageResult searchAndConvert(String name, int page, int pageSize, Long categoryId, Integer status, boolean includeDishes);

    void deleteSetmealIndex(Long setmealId);
}
