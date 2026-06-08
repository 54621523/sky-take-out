package com.sky.product.service.impl.searchengine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.product.domain.document.DishDocument;
import com.sky.product.domain.po.Category;
import com.sky.product.domain.po.Dish;
import com.sky.product.domain.po.DishFlavor;
import com.sky.product.mapper.CategoryMapper;
import com.sky.product.mapper.DishFlavorMapper;
import com.sky.product.mapper.DishMapper;
import com.sky.product.respository.DishSearchRepository;
import com.sky.product.service.searchengine.DishSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DishSyncServiceImpl implements DishSyncService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private DishSearchRepository dishSearchRepository;

    @Override
    public void syncAllDishesToMeilisearch() {
        log.info("开始同步所有菜品到 Meilisearch...");
        List<Dish> dishes = dishMapper.selectList(null);
        List<DishDocument> dishDocuments = new ArrayList<>();

        for (Dish dish : dishes) {
            DishDocument document = convertToDocument(dish);
            dishDocuments.add(document);
        }

        dishSearchRepository.addDishes(dishDocuments);
        log.info("同步完成，共同步 {} 个菜品", dishDocuments.size());
    }

    @Override
    public void syncDishToMeilisearch(Long dishId) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish != null) {
            DishDocument document = convertToDocument(dish);
            dishSearchRepository.updateDish(document);
            log.info("同步菜品到 Meilisearch: dishId={}", dishId);
        }
    }

    @Override
    public void removeDishFromMeilisearch(Long dishId) {
        dishSearchRepository.deleteDish(dishId);
        log.info("从 Meilisearch 删除菜品: dishId={}", dishId);
    }

    private DishDocument convertToDocument(Dish dish) {
        Category category = categoryMapper.selectById(dish.getCategoryId());
        String categoryName = category != null ? category.getName() : "";

        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorMapper.selectList(wrapper);

        List<DishDocument.FlavorDocument> flavorDocuments = flavors.stream()
                .map(this::convertFlavorToDocument)
                .collect(Collectors.toList());

        return DishDocument.builder()
                .id(dish.getId())
                .name(dish.getName())
                .categoryId(dish.getCategoryId())
                .categoryName(categoryName)
                .price(dish.getPrice())
                .image(dish.getImage())
                .description(dish.getDescription())
                .status(dish.getStatus())
                .updateTime(dish.getUpdateTime())
                .flavors(flavorDocuments.isEmpty() ? Collections.emptyList() : flavorDocuments)
                .build();
    }

    private DishDocument.FlavorDocument convertFlavorToDocument(DishFlavor flavor) {
        return DishDocument.FlavorDocument.builder()
                .id(flavor.getId())
                .dishId(flavor.getDishId())
                .name(flavor.getName())
                .value(flavor.getValue())
                .build();
    }
}
