package com.sky.cart.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.cart.domain.po.ShoppingCart;
import com.sky.cart.dto.ShoppingCartDTO;
import com.sky.cart.vo.ShoppingCartVO;

import java.util.List;

public interface ShoppingCartService extends IService<ShoppingCart> {
    List<ShoppingCartVO> listByUserId(Long userId);

    void save(ShoppingCartDTO shoppingCartDTO);

    void delete(ShoppingCartDTO shoppingCartDTO);

    void clean();
}
