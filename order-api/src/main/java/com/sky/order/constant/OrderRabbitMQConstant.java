package com.sky.order.constant;

public interface OrderRabbitMQConstant {

    String ORDER_EXCHANGE = "order.exchange";

    // 订单超时
    String ORDER_TIMEOUT_ROUTING_KEY = "order.timeout";
    String ORDER_TIMEOUT_QUEUE = "order.timeout.queue";
    //订单超时后进入死信队列
    String ORDER_TIMEOUT_DLX_ROUTING_KEY = "order.timeout.dead";
    String ORDER_TIMEOUT_DLX_QUEUE = "order.timeout.dlx.queue";
}
