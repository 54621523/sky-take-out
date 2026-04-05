package com.sky.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.product.domain.po.Setmeal;
import com.sky.product.domain.po.SetmealDish;
import com.sky.product.dto.SetmealDTO;
import com.sky.product.dto.SetmealPageQueryDTO;
import com.sky.product.dubboService.SetmealDubboService;
import com.sky.product.mapper.DishMapper;
import com.sky.product.mapper.SetmealDishMapper;
import com.sky.product.mapper.SetmealMapper;
import com.sky.product.mapper.mapstruct.ProductMapper;
import com.sky.product.service.SetmealService;
import com.sky.product.vo.SetmealDishVO;
import com.sky.product.vo.SetmealOverViewVO;
import com.sky.product.vo.SetmealVO;
import com.sky.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@DubboService(interfaceClass = SetmealDubboService.class)
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper,Setmeal> implements SetmealService, SetmealDubboService {


    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = ProductMapper.INSTANCE.setmealDto2Po(setmealDTO);
        setmealMapper.insert(setmeal);

        List<SetmealDish> setmealDishes = ProductMapper.INSTANCE.setmealDishDto2Po(setmealDTO.getSetmealDishes());
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId()));
        setmealDishMapper.insert(setmealDishes);
    }

    @Override
    public SetmealVO getById(Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        SetmealVO setmealVO = ProductMapper.INSTANCE.setmealPo2Vo(setmeal);

        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(
                new LambdaQueryWrapper<SetmealDish>()
                .eq(SetmealDish::getSetmealId, setmeal.getId()));
        setmealVO.setSetmealDishes(ProductMapper.INSTANCE.setmealDishPo2Vo(setmealDishes));
        return setmealVO;
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<Setmeal> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        List<SetmealVO> records = ProductMapper.INSTANCE.setmealPo2Vo(page.getResult());
        return new PageResult(page.getTotal(),records);
    }

    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = ProductMapper.INSTANCE.setmealDto2Po(setmealDTO);

        setmealMapper.updateById(setmeal);

        List<SetmealDish> setmealDishes = ProductMapper.INSTANCE.setmealDishDto2Po(setmealDTO.getSetmealDishes());
        if(setmealDishes != null && !setmealDishes.isEmpty()){
            setmealDishMapper.delete(new LambdaQueryWrapper<SetmealDish>()
                    .eq(SetmealDish::getSetmealId,setmeal.getId()));
            setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId()));
            setmealDishMapper.insert(setmealDishes);
        }
    }

    @Override
    @Transactional
    public void startOrStop(Integer status, Long id) {
        if(status == 1){
            List<SetmealDish> setmealDishes = setmealDishMapper.selectList(new LambdaQueryWrapper<SetmealDish>()
                    .eq(SetmealDish::getSetmealId,id));

            if(setmealDishes != null && !setmealDishes.isEmpty()){
                for(SetmealDish setmealDish:setmealDishes){
                    if(dishMapper.selectById(setmealDish.getDishId()).getStatus() == 0 ){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                }
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.updateById(setmeal);
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {

        List<Setmeal> setmeals = setmealMapper.selectByIds(ids);
        for(Setmeal setmeal:setmeals){
            if(setmeal.getStatus() == 1){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
            setmealDishMapper.delete(new LambdaQueryWrapper<SetmealDish>()
                    .in(SetmealDish::getSetmealId,ids));
            setmealMapper.deleteByIds(ids);
    }

    @Override
    public List<SetmealVO> list(Long categoryId) {
        return ProductMapper.INSTANCE.setmealPo2Vo(setmealMapper.selectList(new LambdaQueryWrapper<Setmeal>()
                .eq(Setmeal::getCategoryId,categoryId)));
    }

    @Override
    public List<SetmealDishVO> getSetmealDishById(Long id){
        return ProductMapper.INSTANCE.setmealDishPo2Vo(setmealDishMapper.selectList(new LambdaQueryWrapper<SetmealDish>()
                .eq(SetmealDish::getSetmealId,id))
        );
    }

    public SetmealOverViewVO getOverViewSetmeals(){
        return setmealMapper.getOverViewSetmeals();
    }
}
