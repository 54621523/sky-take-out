package com.sky.product.controller;

import com.sky.product.service.searchengine.SetmealSyncService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("adminSetmealSyncController")
@RequestMapping("/admin/setmeal/sync")
@Api(tags = "套餐搜索同步管理接口")
public class SetmealSyncController {

    @Autowired
    private SetmealSyncService setmealSyncService;

    @PostMapping("/all")
    @ApiOperation("同步所有套餐到Meilisearch")
    public String syncAllSetmeals() {
        try {
            log.info("接收到同步所有套餐的请求");
            setmealSyncService.syncAllSetmealsToMeilisearch();
            return "同步成功";
        } catch (Exception e) {
            log.error("同步所有套餐失败", e);
            return "同步失败: " + e.getMessage();
        }
    }

    @PostMapping("/{setmealId}")
    @ApiOperation("同步单个套餐到Meilisearch")
    public String syncSetmeal(@PathVariable Long setmealId) {
        try {
            log.info("接收到同步单个套餐的请求: setmealId={}", setmealId);
            setmealSyncService.syncSetmealToMeilisearch(setmealId);
            return "同步成功: setmealId=" + setmealId;
        } catch (Exception e) {
            log.error("同步套餐失败: setmealId={}", setmealId, e);
            return "同步失败: " + e.getMessage();
        }
    }

    @DeleteMapping("/{setmealId}")
    @ApiOperation("从Meilisearch删除套餐")
    public String deleteSetmeal(@PathVariable Long setmealId) {
        try {
            log.info("接收到删除套餐索引的请求: setmealId={}", setmealId);
            setmealSyncService.removeSetmealFromMeilisearch(setmealId);
            return "删除成功: setmealId=" + setmealId;
        } catch (Exception e) {
            log.error("删除套餐索引失败: setmealId={}", setmealId, e);
            return "删除失败: " + e.getMessage();
        }
    }
}
