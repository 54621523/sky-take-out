package com.sky.cart.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.cart.domain.po.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {

    void updateNumberById(Long id,Integer number);
}
