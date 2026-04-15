package com.sky.cart.constant;

public interface CartRabbitMQConstant {

    String ORDER_NOTIFY_EXCHANGE = "order.notify.exchange";

    // 订单提交后清空购物车
    String ORDER_CART_CLEAR_ROUTING_KEY = "order.cart.clear";
    String ORDER_CART_CLEAR_QUEUE = "order.cart.clear.queue";


}
