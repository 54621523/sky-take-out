package com.sky.product.controller;

import com.sky.product.service.DishSyncService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("adminDishSyncController")
@RequestMapping("/admin/dish/sync")
@Api(tags = "菜品搜索同步管理接口")
public class DishSyncController {

    @Autowired
    private DishSyncService dishSyncService;

    @PostMapping("/all")
    @ApiOperation("同步所有菜品到Meilisearch")
    public String syncAllDishes() {
        try {
            log.info("接收到同步所有菜品的请求");
            dishSyncService.syncAllDishesToMeilisearch();
            return "同步成功";
        } catch (Exception e) {
            log.error("同步所有菜品失败", e);
            return "同步失败: " + e.getMessage();
        }
    }

    @PostMapping("/{dishId}")
    @ApiOperation("同步单个菜品到Meilisearch")
    public String syncDish(@PathVariable Long dishId) {
        try {
            log.info("接收到同步单个菜品的请求: dishId={}", dishId);
            dishSyncService.syncDishToMeilisearch(dishId);
            return "同步成功: dishId=" + dishId;
        } catch (Exception e) {
            log.error("同步菜品失败: dishId={}", dishId, e);
            return "同步失败: " + e.getMessage();
        }
    }

    @DeleteMapping("/{dishId}")
    @ApiOperation("从Meilisearch删除菜品")
    public String removeDish(@PathVariable Long dishId) {
        try {
            log.info("接收到删除菜品的请求: dishId={}", dishId);
            dishSyncService.removeDishFromMeilisearch(dishId);
            return "删除成功: dishId=" + dishId;
        } catch (Exception e) {
            log.error("删除菜品失败: dishId={}", dishId, e);
            return "删除失败: " + e.getMessage();
        }
    }
}
