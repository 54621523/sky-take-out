package com.sky.product.service.impl;

import com.meilisearch.sdk.model.SearchResultPaginated;
import com.sky.product.respository.DishSearchRepository;
import com.sky.product.service.DishSearchService;
import com.sky.product.vo.DishVO;
import com.sky.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DishSearchServiceImpl implements DishSearchService {

    @Autowired
    private DishSearchRepository dishSearchRepository;

    @Override
    public PageResult searchAndConvert(String name, int page, int pageSize, Long categoryId, Integer status, boolean includeFlavors) {
        try {
            SearchResultPaginated searchResult = dishSearchRepository.searchDishes(
                    name, page, pageSize, categoryId, status
            );

            log.info("Meilisearch搜索成功，结果数: {}", searchResult.getHits().size());
            List<DishVO> dishList = convertSearchResultToDishVO(searchResult, includeFlavors);

            return new PageResult(searchResult.getTotalHits(), dishList);

        } catch (Exception e) {
            log.error("Meilisearch搜索失败", e);
            throw new RuntimeException("搜索服务异常", e);
        }
    }

    @Override
    public void deleteDishIndex(Long dishId) {
        try {
            dishSearchRepository.deleteDish(dishId);
        } catch (Exception e) {
            log.error("从Meilisearch删除菜品失败: dishId={}", dishId, e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<DishVO> convertSearchResultToDishVO(SearchResultPaginated searchResult, boolean includeFlavors) {
        List<?> hits = searchResult.getHits();

        return hits.stream().map(hitObj -> {
            Map<String, Object> hit = (Map<String, Object>) hitObj;
            DishVO dishVO = new DishVO();

            if (hit.get("id") != null) {
                dishVO.setId(((Number) hit.get("id")).longValue());
            }
            if (hit.get("name") != null) {
                dishVO.setName((String) hit.get("name"));
            }
            if (hit.get("categoryId") != null) {
                dishVO.setCategoryId(((Number) hit.get("categoryId")).longValue());
            }
            if (hit.get("categoryName") != null) {
                dishVO.setCategoryName((String) hit.get("categoryName"));
            }

            Object priceObj = hit.get("price");
            if (priceObj instanceof Number) {
                dishVO.setPrice(new BigDecimal(priceObj.toString()));
            }

            if (hit.get("image") != null) {
                dishVO.setImage((String) hit.get("image"));
            }
            if (hit.get("description") != null) {
                dishVO.setDescription((String) hit.get("description"));
            }

            Object statusObj = hit.get("status");
            if (statusObj instanceof Number) {
                dishVO.setStatus(((Number) statusObj).intValue());
            }

            if (hit.get("updateTime") != null) {
                String updateTimeStr = hit.get("updateTime").toString();
                try {
                    dishVO.setUpdateTime(LocalDateTime.parse(updateTimeStr));
                } catch (Exception e) {
                    log.warn("解析updateTime失败: {}", updateTimeStr);
                }
            }

            if (includeFlavors && hit.get("flavors") != null) {
                List<Map<String, Object>> flavorsList = (List<Map<String, Object>>) hit.get("flavors");
                List<com.sky.product.vo.DishFlavorVO> flavorVOList = flavorsList.stream()
                        .map(flavorMap -> {
                            com.sky.product.vo.DishFlavorVO flavorVO = new com.sky.product.vo.DishFlavorVO();
                            if (flavorMap.get("id") != null) {
                                flavorVO.setId(((Number) flavorMap.get("id")).longValue());
                            }
                            if (flavorMap.get("dishId") != null) {
                                flavorVO.setDishId(((Number) flavorMap.get("dishId")).longValue());
                            }
                            if (flavorMap.get("name") != null) {
                                flavorVO.setName((String) flavorMap.get("name"));
                            }
                            if (flavorMap.get("value") != null) {
                                flavorVO.setValue((String) flavorMap.get("value"));
                            }
                            return flavorVO;
                        })
                        .collect(Collectors.toList());
                dishVO.setFlavors(flavorVOList);
            }

            return dishVO;
        }).toList();
    }
}
