package com.sky.product.respository;

import com.meilisearch.sdk.model.SearchResultPaginated;
import com.sky.product.domain.document.SetmealDocument;
import com.sky.product.utils.MeilisearchTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class SetmealSearchRepository {

    private static final String INDEX_UID = "setmeal";
    private static final String PRIMARY_KEY = "id";

    @Autowired
    private MeilisearchTemplate meilisearchTemplate;

    public void initIndex() {
        meilisearchTemplate.createIndex(INDEX_UID, PRIMARY_KEY);
        String[] searchableAttributes = {"name", "description", "categoryName", "setmealDishes.name"};
        String[] filterableAttributes = {"categoryId", "categoryName", "status", "price"};
        String[] sortableAttributes = {"price", "updateTime"};
        meilisearchTemplate.updateSettings(INDEX_UID, searchableAttributes, filterableAttributes, sortableAttributes);
    }

    public void addSetmeal(SetmealDocument setmealDocument) {
        List<SetmealDocument> documents = new ArrayList<>();
        documents.add(setmealDocument);
        meilisearchTemplate.addDocuments(INDEX_UID, documents, PRIMARY_KEY);
    }

    public void addSetmeals(List<SetmealDocument> setmealDocuments) {
        meilisearchTemplate.addDocuments(INDEX_UID, setmealDocuments, PRIMARY_KEY);
    }

    public void updateSetmeal(SetmealDocument setmealDocument) {
        List<SetmealDocument> documents = new ArrayList<>();
        documents.add(setmealDocument);
        meilisearchTemplate.updateDocuments(INDEX_UID, documents, PRIMARY_KEY);
    }

    public void deleteSetmeal(Long setmealId) {
        meilisearchTemplate.deleteDocument(INDEX_UID, String.valueOf(setmealId));
    }

    public SearchResultPaginated searchSetmeals(String name, int page, int pageSize, Long categoryId, Integer status) {
        List<String> filters = new ArrayList<>();

        if (categoryId != null) {
            filters.add("categoryId=" + categoryId);
        }

        if (status != null) {
            filters.add("status=" + status);
        }

        String[] filterArray = filters.toArray(new String[0]);

        return meilisearchTemplate.search(INDEX_UID, name, page, pageSize, filterArray);
    }

    public SearchResultPaginated smartSearch(String keyword, int page, int pageSize, Long categoryId, Integer status,
                                             BigDecimal minPrice, BigDecimal maxPrice) {
        List<String> filters = new ArrayList<>();

        if (categoryId != null && categoryId > 0) {
            filters.add("categoryId=" + categoryId);
        }
        if (status != null) {
            filters.add("status=" + status);
        }
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) > 0) {
            filters.add("price >= " + minPrice);
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) {
            filters.add("price <= " + maxPrice);
        }

        String[] filterArray = filters.toArray(new String[0]);
        return meilisearchTemplate.search(INDEX_UID, keyword, page, pageSize, filterArray);
    }


}
