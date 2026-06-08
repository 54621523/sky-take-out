package com.sky.product.service.searchengine;

public interface DishSyncService {

    void syncAllDishesToMeilisearch();

    void syncDishToMeilisearch(Long dishId);

    void removeDishFromMeilisearch(Long dishId);
}
