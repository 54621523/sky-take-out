package com.sky.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.product.domain.po.Dish;
import com.sky.product.domain.po.Setmeal;
import com.sky.product.domain.po.SetmealDish;
import com.sky.product.dto.SetmealDTO;
import com.sky.product.dto.SetmealPageQueryDTO;
import com.sky.product.dubboService.SetmealDubboService;
import com.sky.product.mapper.DishMapper;
import com.sky.product.mapper.SetmealDishMapper;
import com.sky.product.mapper.SetmealMapper;
import com.sky.product.mapper.mapstruct.ProductMapper;
import com.sky.product.service.SetmealSearchMessageProducer;
import com.sky.product.service.SetmealSearchService;
import com.sky.product.service.SetmealService;
import com.sky.product.service.SetmealSyncService;
import com.sky.product.vo.SetmealDishVO;
import com.sky.product.vo.SetmealOverViewVO;
import com.sky.product.vo.SetmealVO;
import com.sky.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@DubboService(interfaceClass = SetmealDubboService.class)
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper,Setmeal> implements SetmealService, SetmealDubboService {


    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealSyncService setmealSyncService;
    @Autowired
    private SetmealSearchService setmealSearchService;
    @Autowired
    private SetmealSearchMessageProducer setmealSearchMessageProducer;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = ProductMapper.INSTANCE.setmealDto2Po(setmealDTO);
        setmealMapper.insert(setmeal);

        List<SetmealDish> setmealDishes = ProductMapper.INSTANCE.setmealDishDto2Po(setmealDTO.getSetmealDishes());
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId()));
        setmealDishMapper.insert(setmealDishes);
        setmealSearchMessageProducer.sendSyncMessage(setmeal.getId());
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
        try {
            return setmealSearchService.searchAndConvert(
                    setmealPageQueryDTO.getName(),
                    setmealPageQueryDTO.getPage(),
                    setmealPageQueryDTO.getPageSize(),
                    setmealPageQueryDTO.getCategoryId() != null ? Long.valueOf(setmealPageQueryDTO.getCategoryId()) : null,
                    setmealPageQueryDTO.getStatus(),
                    false
            );
        } catch (Exception e) {
            log.warn("Meilisearch搜索失败，降级到MySQL: {}", e.getMessage());
        }


        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),new ArrayList<>(page.getResult()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        setmealSearchMessageProducer.sendSyncMessage(setmeal.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startOrStop(Integer status, Long id) {
        if(status == 1){
            List<SetmealDish> setmealDishes = setmealDishMapper.selectList(new LambdaQueryWrapper<SetmealDish>()
                    .eq(SetmealDish::getSetmealId,id));

            if(setmealDishes != null && !setmealDishes.isEmpty()){
                List<Long> dishIds = setmealDishes.stream()
                        .map(SetmealDish::getDishId)
                        .toList();

                long disabledCount = dishMapper.selectCount(
                        new LambdaQueryWrapper<Dish>()
                                .in(Dish::getId, dishIds)
                                .eq(Dish::getStatus, 0));

                if(disabledCount > 0){
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.updateById(setmeal);
        setmealSearchMessageProducer.sendSyncMessage(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        ids.forEach(id -> setmealSearchMessageProducer.sendDeleteMessage(id));
    }

    @Cacheable(cacheNames = "setmealCache", key = "#categoryId")
    @Override
    public List<SetmealVO> list(Long categoryId) {
        try {
            PageResult pageResult = setmealSearchService.searchAndConvert(
                    null, 1, 100, categoryId, 1, false
            );
            List<SetmealVO> setmeals = pageResult.getRecords();
            return setmeals.isEmpty() ? Collections.emptyList() : setmeals;
        } catch (Exception e) {
            log.warn("Meilisearch搜索失败，降级到MySQL: {}", e.getMessage());
        }

        return ProductMapper.INSTANCE.setmealPo2Vo(setmealMapper.selectList(new LambdaQueryWrapper<Setmeal>()
                .eq(Setmeal::getCategoryId,categoryId)));
    }

    @Override
    public List<SetmealDishVO> getSetmealDishById(Long id){
        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(new LambdaQueryWrapper<SetmealDish>()
                .eq(SetmealDish::getSetmealId,id));

        if(setmealDishes == null || setmealDishes.isEmpty()){
            return new java.util.ArrayList<>();
        }

        List<Long> dishIds = setmealDishes.stream()
                .map(SetmealDish::getDishId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, Dish> dishMap = new java.util.HashMap<>();
        if(!dishIds.isEmpty()){
            List<Dish> dishes = dishMapper.selectList(
                    new LambdaQueryWrapper<Dish>()
                            .in(Dish::getId, dishIds));
            dishMap = dishes.stream()
                    .collect(java.util.stream.Collectors.toMap(
                            Dish::getId,
                            dish -> dish));
        }

        Map<Long,Dish> finalDishMap = dishMap;

        return setmealDishes.stream()
                .map(setmealDish -> {
                    SetmealDishVO vo = ProductMapper.INSTANCE.setmealDishPo2Vo(setmealDish);

                    if (setmealDish.getDishId() != null) {
                        Dish dish = finalDishMap.get(setmealDish.getDishId());
                        if (dish != null) {
                            vo.setDescription(dish.getDescription() != null ? dish.getDescription() : "");
                            vo.setImage(dish.getImage() != null ? dish.getImage() : "");
                        } else {
                            vo.setDescription("");
                            vo.setImage("");
                        }
                    } else {
                        vo.setDescription("");
                        vo.setImage("");
                    }

                    return vo;
                })
                .toList();
    }

    public SetmealOverViewVO getOverViewSetmeals(){
        return setmealMapper.getOverViewSetmeals();
    }
}
