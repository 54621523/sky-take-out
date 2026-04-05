package com.sky.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.cart.domain.po.ShoppingCart;
import com.sky.cart.dto.ShoppingCartDTO;
import com.sky.cart.dubboService.CartDubboService;
import com.sky.cart.mapper.ShoppingCartMapper;
import com.sky.cart.mapper.mapstruct.CartMapStruct;
import com.sky.cart.service.ShoppingCartService;
import com.sky.cart.vo.ShoppingCartVO;
import com.sky.context.BaseContext;
import com.sky.product.dubboService.DishDubboService;
import com.sky.product.dubboService.SetmealDubboService;
import com.sky.product.vo.DishVO;
import com.sky.product.vo.SetmealVO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@DubboService(interfaceClass = CartDubboService.class)
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper,ShoppingCart> implements ShoppingCartService, CartDubboService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @DubboReference
    private DishDubboService dishService;
    @DubboReference
    private SetmealDubboService setmealService;

    @Override
    public List<ShoppingCartVO> listByUserId(Long userId) {
        return CartMapStruct.INSTANCE.shoppingCartPo2Vo(shoppingCartMapper.selectList(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId,userId)));
    }

    @Override
    public void save(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = CartMapStruct.INSTANCE.shoppingCartDto2Po(shoppingCartDTO);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        
        ShoppingCart existCart = isSameItemInCart(userId, shoppingCart);
        if(existCart != null) {
            shoppingCartMapper.updateNumberById(existCart.getId(),existCart.getNumber() + 1);
        }else{
            //全新菜品或者套餐
            //取得菜品或是套餐的id
            if(shoppingCart.getDishId() != null){
                DishVO dish = dishService.getById(shoppingCart.getDishId());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setName(dish.getName());
                shoppingCart.setSetmealId(null);
            }
            if(shoppingCart.getSetmealId() != null){
                SetmealVO setmeal = setmealService.getById(shoppingCart.getSetmealId());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setDishId(null);
            }
            shoppingCart.setNumber(1);
            shoppingCartMapper.insert(shoppingCart);
        }


    }

    @Override
    public void delete(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = CartMapStruct.INSTANCE.shoppingCartDto2Po(shoppingCartDTO);
        shoppingCart.setUserId(userId);

        ShoppingCart existCart = isSameItemInCart(userId, shoppingCart);
        if(existCart != null) {
            if(existCart.getNumber() > 1){
                shoppingCartMapper.updateNumberById(existCart.getId(),existCart.getNumber() - 1);
            }
            else{
                shoppingCartMapper.deleteById(existCart);
            }
        }
    }


    @Override
    public void clean() {

        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.delete(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId,userId));
    }


    private ShoppingCart isSameItemInCart(Long userId, ShoppingCart shoppingCart) {
        if(shoppingCart.getDishId() != null) {
            LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<ShoppingCart>()
                    .eq(ShoppingCart::getUserId, userId)
                    .eq(ShoppingCart::getDishId, shoppingCart.getDishId());

            if (shoppingCart.getDishFlavor() != null) {
                wrapper.eq(ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor());
            }

            List<ShoppingCart> carts = shoppingCartMapper.selectList(wrapper);
            return carts.isEmpty() ? null : carts.get(0);
        } else if(shoppingCart.getSetmealId() != null) {
            List<ShoppingCart> carts = shoppingCartMapper.selectList(new LambdaQueryWrapper<ShoppingCart>()
                    .eq(ShoppingCart::getUserId, userId)
                    .eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId()));
            return carts.isEmpty() ? null : carts.get(0);
        }
        return null;
    }
}
