package com.sky.product.dubboService;

import com.sky.product.dto.SetmealDTO;
import com.sky.product.dto.SetmealPageQueryDTO;
import com.sky.product.vo.SetmealDishVO;
import com.sky.product.vo.SetmealOverViewVO;
import com.sky.product.vo.SetmealVO;
import com.sky.result.PageResult;

import java.math.BigDecimal;
import java.util.List;

public interface SetmealDubboService {

    /**
     * 新增套餐
     * @param setmealDTO
     */
    void save(SetmealDTO setmealDTO);

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据 id 查询套餐
     * @param id
     * @return
     */
    SetmealVO getById(Long id);

    /**
     * 启用、禁用套餐
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 修改套餐
     * @param setmealDTO
     */
    void update(SetmealDTO setmealDTO);

    /**
     * 删除套餐
     * @param ids
     */
    void delete(List<Long> ids);

    /**
     * 根据分类 ID 查询套餐列表
     * @param categoryId
     * @return
     */
    List<SetmealVO> list(Long categoryId);

    /**
     * 根据套餐Id查询套餐所含菜品
     * @param id
     * @return
     */
    List<SetmealDishVO> getSetmealDishById(Long id);

    /**
     * 关键词搜索套餐（含包含菜品数据），支持分类和价格过滤
     * Meilisearch全文检索：匹配套餐名、描述、包含菜品名
     */
    PageResult searchByKeyword(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, int page, int pageSize);


    SetmealOverViewVO getOverViewSetmeals();
}
