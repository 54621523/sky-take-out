package com.sky.product.service.impl.searchengine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.product.domain.document.SetmealDocument;
import com.sky.product.domain.po.Category;
import com.sky.product.domain.po.Setmeal;
import com.sky.product.domain.po.SetmealDish;
import com.sky.product.mapper.CategoryMapper;
import com.sky.product.mapper.SetmealDishMapper;
import com.sky.product.mapper.SetmealMapper;
import com.sky.product.respository.SetmealSearchRepository;
import com.sky.product.service.searchengine.SetmealSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SetmealSyncServiceImpl implements SetmealSyncService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private SetmealSearchRepository setmealSearchRepository;

    @Override
    public void syncAllSetmealsToMeilisearch() {
        log.info("开始同步所有套餐到 Meilisearch...");
        List<Setmeal> setmeals = setmealMapper.selectList(null);
        List<SetmealDocument> setmealDocuments = new ArrayList<>();

        for (Setmeal setmeal : setmeals) {
            SetmealDocument document = convertToDocument(setmeal);
            setmealDocuments.add(document);
        }

        setmealSearchRepository.addSetmeals(setmealDocuments);
        log.info("同步完成，共同步 {} 个套餐", setmealDocuments.size());
    }

    @Override
    public void syncSetmealToMeilisearch(Long setmealId) {
        Setmeal setmeal = setmealMapper.selectById(setmealId);
        if (setmeal != null) {
            SetmealDocument document = convertToDocument(setmeal);
            setmealSearchRepository.updateSetmeal(document);
            log.info("同步套餐到 Meilisearch: setmealId={}", setmealId);
        }
    }

    @Override
    public void removeSetmealFromMeilisearch(Long setmealId) {
        setmealSearchRepository.deleteSetmeal(setmealId);
        log.info("从 Meilisearch 删除套餐: setmealId={}", setmealId);
    }

    private SetmealDocument convertToDocument(Setmeal setmeal) {
        Category category = categoryMapper.selectById(setmeal.getCategoryId());
        String categoryName = category != null ? category.getName() : "";

        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId, setmeal.getId());
        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(wrapper);

        List<SetmealDocument.SetmealDishDocument> setmealDishDocuments = setmealDishes.stream()
                .map(this::convertSetmealDishToDocument)
                .collect(Collectors.toList());

        return SetmealDocument.builder()
                .id(setmeal.getId())
                .name(setmeal.getName())
                .categoryId(setmeal.getCategoryId())
                .categoryName(categoryName)
                .price(setmeal.getPrice())
                .image(setmeal.getImage())
                .description(setmeal.getDescription())
                .status(setmeal.getStatus())
                .updateTime(setmeal.getUpdateTime())
                .setmealDishes(setmealDishDocuments.isEmpty() ? Collections.emptyList() : setmealDishDocuments)
                .build();
    }

    private SetmealDocument.SetmealDishDocument convertSetmealDishToDocument(SetmealDish setmealDish) {
        return SetmealDocument.SetmealDishDocument.builder()
                .id(setmealDish.getId())
                .setmealId(setmealDish.getSetmealId())
                .dishId(setmealDish.getDishId())
                .name(setmealDish.getName())
                .price(setmealDish.getPrice())
                .copies(setmealDish.getCopies())
                .build();
    }
}
