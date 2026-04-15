package com.sky.product.service;

import com.meilisearch.sdk.model.SearchResultPaginated;
import com.sky.product.vo.DishVO;
import com.sky.result.PageResult;

import java.util.List;

public interface DishSearchService {

    /**
     * 搜索菜品并转换为VO列表
     * @param name 菜品名称
     * @param page 页码
     * @param pageSize 每页数量
     * @param categoryId 分类ID
     * @param status 状态
     * @param includeFlavors 是否包含口味数据
     * @return 菜品VO列表
     */
    PageResult searchAndConvert(String name, int page, int pageSize, Long categoryId, Integer status, boolean includeFlavors);

    /**
     * 删除菜品索引
     * @param dishId 菜品ID
     */
    void deleteDishIndex(Long dishId);
}
