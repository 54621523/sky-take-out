package com.sky.websocket.constant;

public interface WebSocketRabbitMQConstant {



    String ORDER_NOTIFY_EXCHANGE = "order.notify.exchange";

    // 订单支付成功
    String ORDER_PAYMENT_SUCCESS_ROUTING_KEY = "order.payment.success";
    String ORDER_PAYMENT_SUCCESS_QUEUE = "order.payment.success.queue";

    // 订单催单
    String ORDER_REMINDER_ROUTING_KEY = "order.reminder";
    String ORDER_REMINDER_QUEUE = "order.reminder.queue";

    //客服
    String ORDER_CUSTOMER_SERVICE_ROUTING_KEY = "order.customer.service";
    String ORDER_CUSTOMER_SERVICE_QUEUE = "order.customer.service.queue";
}
