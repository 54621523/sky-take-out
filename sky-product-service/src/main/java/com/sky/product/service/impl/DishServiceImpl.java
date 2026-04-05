package com.sky.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.product.dto.DishDTO;
import com.sky.product.dto.DishPageQueryDTO;
import com.sky.product.dubboService.DishDubboService;
import com.sky.product.vo.DishOverViewVO;
import com.sky.product.vo.DishVO;
import com.sky.result.PageResult;
import com.sky.product.domain.po.Dish;
import com.sky.product.domain.po.DishFlavor;
import com.sky.product.domain.po.SetmealDish;
import lombok.extern.slf4j.Slf4j;
import com.sky.product.mapper.DishFlavorMapper;
import com.sky.product.mapper.DishMapper;
import com.sky.product.mapper.mapstruct.ProductMapper;
import com.sky.product.mapper.SetmealDishMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.sky.product.service.DishService;

import java.util.List;

@DubboService(interfaceClass = DishDubboService.class)
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper,Dish> implements DishService, DishDubboService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Override
    @Transactional
    public void save(DishDTO dishDTO) {
        Dish dish = ProductMapper.INSTANCE.dishDto2Po(dishDTO);

        dish.setStatus(StatusConstant.DISABLE);
        dishMapper.insert(dish);

        List<DishFlavor> flavors = ProductMapper.INSTANCE.dishFlavorDto2Po(dishDTO.getFlavors());
        if(flavors != null && !flavors.isEmpty()){
                flavors.forEach( flavor -> flavor.setDishId(dish.getId()));
                dishFlavorMapper.insert(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<Dish> page = dishMapper.pageQuery(dishPageQueryDTO);
        List<DishVO> records = ProductMapper.INSTANCE.dishPo2Vo(page.getResult());
        return new PageResult(page.getTotal(),records);
    }

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    @Override
    public DishVO getById(Long id) {
        Dish dish = dishMapper.selectById(id);

        List<DishFlavor> flavors = dishFlavorMapper.selectList(new LambdaQueryWrapper<DishFlavor>()
                .eq(DishFlavor::getDishId,id));

        DishVO dishVO = ProductMapper.INSTANCE.dishPo2Vo(dish);
        if (flavors != null && !flavors.isEmpty()){
            dishVO.setFlavors(ProductMapper.INSTANCE.dishFlavorPo2Vo(flavors));
        }
        return dishVO;
    }

    /**
     * 起售停售
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .status(status)
                .id(id)
                .build();
        dishMapper.updateById(dish);
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Transactional
    @Override
    public void update(DishDTO dishDTO) {
        Dish dish = ProductMapper.INSTANCE.dishDto2Po(dishDTO);
        dishMapper.updateById(dish);


        List<DishFlavor> flavors = ProductMapper.INSTANCE.dishFlavorDto2Po(dishDTO.getFlavors());
        if(flavors != null && !flavors.isEmpty()){
            flavors.forEach( flavor -> flavor.setDishId(dish.getId()));
            dishFlavorMapper.delete(new LambdaQueryWrapper<DishFlavor>()
                    .eq(DishFlavor::getDishId,dish.getId()));
            dishFlavorMapper.insert(flavors);
        }
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Transactional
    @Override
    public void delete(List<Long> ids) {
        //起售中的菜品不能删除
        for (Long id:ids){
            Dish dish = dishMapper.selectById(id);
            if(dish.getStatus() == 1){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //被套餐关联的菜品不能删除
        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(new LambdaQueryWrapper<SetmealDish>()
                .in(SetmealDish::getSetmealId,ids));
        if (setmealDishes != null && !setmealDishes.isEmpty()){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
            dishMapper.deleteByIds(ids);

            dishFlavorMapper.delete(new LambdaQueryWrapper<DishFlavor>()
                    .in(DishFlavor::getDishId,ids));
    }

    @Override
    public List<DishVO> listWithFlavors(Long categoryId) {

        List<Dish> dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .in(Dish::getCategoryId,categoryId));

       if(dishes.isEmpty()){
           //TODO 错误信息
           throw new BaseException("当前分类无菜品");
       }

        return dishes.stream().map(dish -> {
            DishVO dishVO = ProductMapper.INSTANCE.dishPo2Vo(dish);

            List<DishFlavor> flavors = dishFlavorMapper.selectList(new LambdaQueryWrapper<DishFlavor>()
                            .eq(DishFlavor::getDishId,dish.getId()));

            dishVO.setFlavors(ProductMapper.INSTANCE.dishFlavorPo2Vo(flavors));
            return dishVO;
        }).toList();
    }

    public DishOverViewVO getOverViewDishes(){
        return dishMapper.getOverViewDishes();

    }


}
