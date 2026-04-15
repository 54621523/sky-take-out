package com.sky.product.config;

import com.sky.product.respository.DishSearchRepository;
import com.sky.product.respository.SetmealSearchRepository;
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


    @Override
    public void run(String... args) {
        try {
            log.info("开始初始化 Meilisearch 索引...");
            dishSearchRepository.initIndex();
            setmealSearchRepository.initIndex();
            log.info("Meilisearch 索引初始化完成");
        } catch (Exception e) {
            log.error("Meilisearch 索引初始化失败", e);
        }
    }
}
