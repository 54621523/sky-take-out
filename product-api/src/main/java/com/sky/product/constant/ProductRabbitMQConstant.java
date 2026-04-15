package com.sky.product.constant;

public interface ProductRabbitMQConstant {

    String PRODUCT_EXCHANGE = "product.exchange";

    // 菜品搜索索引同步
    String DISH_SEARCH_SYNC_ROUTING_KEY = "dish.search.sync";
    String DISH_SEARCH_SYNC_QUEUE = "dish.search.sync.queue";

    // 菜品搜索索引删除
    String DISH_SEARCH_DELETE_ROUTING_KEY = "dish.search.delete";
    String DISH_SEARCH_DELETE_QUEUE = "dish.search.delete.queue";
}
