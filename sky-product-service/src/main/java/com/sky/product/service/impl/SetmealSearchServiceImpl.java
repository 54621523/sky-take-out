package com.sky.product.service.impl;

import com.meilisearch.sdk.model.SearchResultPaginated;
import com.sky.product.respository.SetmealSearchRepository;
import com.sky.product.service.SetmealSearchService;
import com.sky.product.vo.SetmealDishVO;
import com.sky.product.vo.SetmealVO;
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
public class SetmealSearchServiceImpl implements SetmealSearchService {

    @Autowired
    private SetmealSearchRepository setmealSearchRepository;

    @Override
    public PageResult searchAndConvert(String name, int page, int pageSize, Long categoryId, Integer status, boolean includeDishes) {
        try {
            SearchResultPaginated searchResult = setmealSearchRepository.searchSetmeals(
                    name, page, pageSize, categoryId, status
            );

            log.info("Meilisearch搜索套餐成功，结果数: {}", searchResult.getHits().size());
            List<SetmealVO> setmealList = convertSearchResultToSetmealVO(searchResult, includeDishes);

            return new PageResult(searchResult.getTotalHits(), setmealList);

        } catch (Exception e) {
            log.error("Meilisearch搜索套餐失败", e);
            throw new RuntimeException("搜索服务异常", e);
        }
    }

    @Override
    public PageResult smartSearch(String keyword, int page, int pageSize, Long categoryId, Integer status,
                                  BigDecimal minPrice, BigDecimal maxPrice) {
        try {
            SearchResultPaginated searchResult = setmealSearchRepository.smartSearch(
                    keyword, page, pageSize, categoryId, status, minPrice, maxPrice
            );

            log.info("Meilisearch智能搜索套餐成功，关键词: {}，结果数: {}", keyword, searchResult.getHits().size());
            List<SetmealVO> setmealList = convertSearchResultToSetmealVO(searchResult, true);
            return new PageResult(searchResult.getTotalHits(), setmealList);

        } catch (Exception e) {
            log.warn("Meilisearch智能搜索套餐失败，降级返回空结果: {}", e.getMessage());
            return new PageResult(0, List.of());
        }
    }

    @Override
    public void deleteSetmealIndex(Long setmealId) {
        try {
            setmealSearchRepository.deleteSetmeal(setmealId);
        } catch (Exception e) {
            log.error("从Meilisearch删除套餐失败: setmealId={}", setmealId, e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<SetmealVO> convertSearchResultToSetmealVO(SearchResultPaginated searchResult, boolean includeDishes) {
        List<?> hits = searchResult.getHits();

        return hits.stream().map(hitObj -> {
            Map<String, Object> hit = (Map<String, Object>) hitObj;
            SetmealVO setmealVO = new SetmealVO();

            if (hit.get("id") != null) {
                setmealVO.setId(((Number) hit.get("id")).longValue());
            }
            if (hit.get("name") != null) {
                setmealVO.setName((String) hit.get("name"));
            }
            if (hit.get("categoryId") != null) {
                setmealVO.setCategoryId(((Number) hit.get("categoryId")).longValue());
            }
            if (hit.get("categoryName") != null) {
                setmealVO.setCategoryName((String) hit.get("categoryName"));
            }

            Object priceObj = hit.get("price");
            if (priceObj instanceof Number) {
                setmealVO.setPrice(new BigDecimal(priceObj.toString()));
            }

            if (hit.get("image") != null) {
                setmealVO.setImage((String) hit.get("image"));
            }
            if (hit.get("description") != null) {
                setmealVO.setDescription((String) hit.get("description"));
            }

            Object statusObj = hit.get("status");
            if (statusObj instanceof Number) {
                setmealVO.setStatus(((Number) statusObj).intValue());
            }

            if (hit.get("updateTime") != null) {
                String updateTimeStr = hit.get("updateTime").toString();
                try {
                    setmealVO.setUpdateTime(LocalDateTime.parse(updateTimeStr));
                } catch (Exception e) {
                    log.warn("解析updateTime失败: {}", updateTimeStr);
                }
            }

            if (includeDishes && hit.get("setmealDishes") != null) {
                List<Map<String, Object>> dishesList = (List<Map<String, Object>>) hit.get("setmealDishes");
                List<SetmealDishVO> dishVOList = dishesList.stream()
                        .map(dishMap -> {
                            SetmealDishVO dishVO = new SetmealDishVO();
                            if (dishMap.get("name") != null) {
                                dishVO.setName((String) dishMap.get("name"));
                            }
                            if (dishMap.get("description") != null) {
                                dishVO.setDescription((String) dishMap.get("description"));
                            }
                            if (dishMap.get("image") != null) {
                                dishVO.setImage((String) dishMap.get("image"));
                            }
                            if (dishMap.get("copies") != null) {
                                dishVO.setCopies(((Number) dishMap.get("copies")).intValue());
                            }
                            return dishVO;
                        })
                        .collect(Collectors.toList());
                setmealVO.setSetmealDishes(dishVOList);
            }

            return setmealVO;
        }).toList();
    }
}
