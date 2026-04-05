package com.sky.cart.dubboService;

import com.sky.cart.dto.ShoppingCartDTO;
import com.sky.cart.vo.ShoppingCartVO;

import java.util.List;

public interface CartDubboService {

    List<ShoppingCartVO> listByUserId(Long userId);

    void save(ShoppingCartDTO shoppingCartDTO);

    void delete(ShoppingCartDTO shoppingCartDTO);

    void clean();

}
