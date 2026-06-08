package com.sky.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.product.domain.po.*;
import com.sky.product.dto.DishDTO;
import com.sky.product.dto.DishPageQueryDTO;
import com.sky.product.dubboService.DishDubboService;
import com.sky.product.mapper.CategoryMapper;
import com.sky.product.service.messageQueue.DishSearchMessageProducer;
import com.sky.product.service.DishSearchService;
import com.sky.product.vo.DishOverViewVO;
import com.sky.product.vo.DishVO;
import com.sky.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import com.sky.product.mapper.DishFlavorMapper;
import com.sky.product.mapper.DishMapper;
import com.sky.product.mapper.mapstruct.ProductMapper;
import com.sky.product.mapper.SetmealDishMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import com.sky.product.service.DishService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@DubboService(interfaceClass = DishDubboService.class)
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper,Dish> implements DishService, DishDubboService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishSearchService dishSearchService;
    @Autowired
    private DishSearchMessageProducer dishSearchMessageProducer;

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(DishDTO dishDTO) {
        Dish dish = ProductMapper.INSTANCE.dishDto2Po(dishDTO);

        dish.setStatus(StatusConstant.DISABLE);
        dishMapper.insert(dish);

        List<DishFlavor> flavors = ProductMapper.INSTANCE.dishFlavorDto2Po(dishDTO.getFlavors());
        if(flavors != null && !flavors.isEmpty()){
                flavors.forEach( flavor -> flavor.setDishId(dish.getId()));
                dishFlavorMapper.insert(flavors);
        }
        dishSearchMessageProducer.sendSyncMessage(dish.getId());
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        try {
            return dishSearchService.searchAndConvert(
                    dishPageQueryDTO.getName(),
                    dishPageQueryDTO.getPage(),
                    dishPageQueryDTO.getPageSize(),
                    dishPageQueryDTO.getCategoryId() != null ? Long.valueOf(dishPageQueryDTO.getCategoryId()) : null,
                    dishPageQueryDTO.getStatus(),
                    false
            );
        }catch (Exception e){
            log.warn("Meilisearch搜索失败，降级到MySQL: {}", e.getMessage());
        }
            PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),new ArrayList<>(page.getResult()));
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
    @Transactional(rollbackFor = Exception.class)
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .status(status)
                .id(id)
                .build();
        dishMapper.updateById(dish);
        dishSearchMessageProducer.sendSyncMessage(id);

    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Transactional(rollbackFor = Exception.class)
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
        dishSearchMessageProducer.sendSyncMessage(dish.getId());
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Transactional(rollbackFor = Exception.class)
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
            for (Long id : ids) {
                dishSearchMessageProducer.sendDeleteMessage(id);
            }
    }

    @Override
    @Cacheable(cacheNames = "dishCache", key = "#categoryId")
    public List<DishVO> listWithFlavors(Long categoryId) {
        try {
            PageResult pageResult = dishSearchService.searchAndConvert(
                    null, 1, 100, categoryId, StatusConstant.ENABLE, true
            );
            List<DishVO> dishes = pageResult.getRecords();
            return dishes.isEmpty() ? Collections.emptyList() : dishes;
        } catch (Exception e) {
            log.warn("Meilisearch搜索失败，降级到MySQL: {}", e.getMessage());
        }

        List<Dish> dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .in(Dish::getCategoryId,categoryId));

        if(dishes.isEmpty()){
            return Collections.emptyList();
        }

        List<Long> dishIds = dishes.stream().map(Dish::getId).toList();

        List<DishFlavor> allFlavors = dishFlavorMapper.selectList(new LambdaQueryWrapper<DishFlavor>()
                .in(DishFlavor::getDishId, dishIds));

        Map<Long, List<DishFlavor>> flavorMap = allFlavors.stream()
                .collect(Collectors.groupingBy(DishFlavor::getDishId));

        List<Long> categoryIds = dishes.stream().map(Dish::getCategoryId).distinct().toList();
        List<Category> categories = categoryMapper.selectByIds(categoryIds);
        Map<Long, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(Category::getId,
                        category -> category.getName() != null ? category.getName() : "",
                        (v1, v2) -> v1));

        return dishes.stream().map(dish -> {
            DishVO dishVO = ProductMapper.INSTANCE.dishPo2Vo(dish);

            List<DishFlavor> flavors = flavorMap.getOrDefault(dish.getId(), Collections.emptyList());
            dishVO.setFlavors(ProductMapper.INSTANCE.dishFlavorPo2Vo(flavors));

            dishVO.setCategoryName(categoryNameMap.getOrDefault(dish.getCategoryId(), ""));

            return dishVO;
        }).toList();
    }

    @Override
    public PageResult searchByKeyword(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, int page, int pageSize) {
        try {
            return dishSearchService.smartSearch(
                    keyword, page, pageSize, categoryId, StatusConstant.ENABLE, minPrice, maxPrice
            );
        } catch (Exception e) {
            log.warn("Meilisearch关键词搜索失败，降级到MySQL: {}", e.getMessage());
        }
        DishPageQueryDTO fallbackDTO = new DishPageQueryDTO();
        fallbackDTO.setName(keyword);
        fallbackDTO.setCategoryId(categoryId != null ? categoryId.intValue() : null);
        fallbackDTO.setStatus(StatusConstant.ENABLE);
        fallbackDTO.setPage(page);
        fallbackDTO.setPageSize(pageSize);
        PageHelper.startPage(page, pageSize);
        Page<DishVO> mysqlPage = dishMapper.pageQuery(fallbackDTO);
        return new PageResult(mysqlPage.getTotal(), new ArrayList<>(mysqlPage.getResult()));
    }

    public DishOverViewVO getOverViewDishes(){
        return dishMapper.getOverViewDishes();

    }


}
