package com.sky.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.product.domain.po.Setmeal;
import com.sky.product.dto.SetmealDTO;
import com.sky.product.dto.SetmealPageQueryDTO;
import com.sky.product.vo.SetmealDishVO;
import com.sky.product.vo.SetmealVO;
import com.sky.result.PageResult;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void save(SetmealDTO setmealDTO);

    SetmealVO getById(Long id);

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void update(SetmealDTO setmealDTO);

    void startOrStop(Integer status, Long id);

    void delete(List<Long> ids);

    List<SetmealVO> list(Long categoryId);

    List<SetmealDishVO> getSetmealDishById(Long id);


}
