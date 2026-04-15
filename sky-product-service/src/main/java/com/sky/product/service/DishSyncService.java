package com.sky.product.service;

import java.util.List;

public interface DishSyncService {

    void syncAllDishesToMeilisearch();

    void syncDishToMeilisearch(Long dishId);

    void removeDishFromMeilisearch(Long dishId);
}
