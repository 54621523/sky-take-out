package com.sky.product.service.searchengine;

public interface SetmealSyncService {

    void syncAllSetmealsToMeilisearch();

    void syncSetmealToMeilisearch(Long setmealId);

    void removeSetmealFromMeilisearch(Long setmealId);
}
