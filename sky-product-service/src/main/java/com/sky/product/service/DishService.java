package com.sky.product.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.product.dto.DishDTO;
import com.sky.product.dto.DishPageQueryDTO;
import com.sky.product.vo.DishOverViewVO;
import com.sky.product.vo.DishVO;
import com.sky.result.PageResult;
import com.sky.product.domain.po.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    void save(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id查询菜品和对应的口味
     * @param id
     * @return
     */
    DishVO getById(Long id);

    /**
     * 启用、禁用菜品
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 修改菜品
     * @param dishDTO
     */
    void update(DishDTO dishDTO);

    /**
     * 删除菜品
     * @param ids
     */
    void delete(List<Long> ids);

    List<DishVO> listWithFlavors(Long categoryId);

    DishOverViewVO getOverViewDishes();
}
