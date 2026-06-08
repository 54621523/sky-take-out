package com.sky.product.config;

import com.sky.product.respository.DishSearchRepository;
import com.sky.product.respository.SetmealSearchRepository;
import com.sky.product.service.searchengine.DishSyncService;
import com.sky.product.service.searchengine.SetmealSyncService;
import com.sky.product.utils.MeilisearchTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MeilisearchInitializer implements CommandLineRunner {

    @Autowired
    private DishSearchRepository dishSearchRepository;
    @Autowired
    private SetmealSearchRepository setmealSearchRepository;
    @Autowired
    private DishSyncService dishSyncService;
    @Autowired
    private SetmealSyncService setmealSyncService;
    @Autowired
    private MeilisearchTemplate meilisearchTemplate;

    @Override
    public void run(String... args) {
        try {
            log.info("开始初始化 Meilisearch 索引...");
            dishSearchRepository.initIndex();
            setmealSearchRepository.initIndex();
            log.info("Meilisearch 索引初始化完成");

            syncIfEmpty("dish", dishSyncService::syncAllDishesToMeilisearch);
            syncIfEmpty("setmeal", setmealSyncService::syncAllSetmealsToMeilisearch);

        } catch (Exception e) {
            log.error("Meilisearch 索引初始化失败", e);
        }
    }

    private void syncIfEmpty(String indexUid, Runnable syncAction) {
        try {
            long docCount = meilisearchTemplate.getIndex(indexUid).getStats().getNumberOfDocuments();
            if (docCount == 0) {
                log.info("索引 {} 为空，自动触发全量同步...", indexUid);
                syncAction.run();
            } else {
                log.info("索引 {} 已有 {} 条文档，跳过自动同步", indexUid, docCount);
            }
        } catch (Exception e) {
            log.warn("检查索引 {} 文档数失败，尝试全量同步: {}", indexUid, e.getMessage());
            try {
                syncAction.run();
            } catch (Exception ex) {
                log.error("全量同步 {} 失败", indexUid, ex);
            }
        }
    }
}