package com.sky.order.service;


import com.sky.order.constant.OrderRabbitMQConstant;
import com.sky.order.dto.OrderCartClearMessage;
import com.sky.order.dto.OrderMessageDTO;
import com.sky.websocket.constant.WebSocketRabbitMQConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendOrderTimeoutMessage(OrderMessageDTO message) {
        log.info("发送订单超时延迟消息, orderId: {}, orderNumber: {}",
                message.getOrderId(), message.getOrderNumber());
        rabbitTemplate.convertAndSend(
                OrderRabbitMQConstant.ORDER_EXCHANGE,
                OrderRabbitMQConstant.ORDER_TIMEOUT_ROUTING_KEY,
                message
        );
        log.info("订单超时延迟消息发送成功");
    }

    public void sendPaymentSuccessMessage(OrderMessageDTO message) {
        log.info("发送支付成功消息, orderId: {}, orderNumber: {}",
                message.getOrderId(), message.getOrderNumber());
        rabbitTemplate.convertAndSend(
                WebSocketRabbitMQConstant.ORDER_NOTIFY_EXCHANGE,
                WebSocketRabbitMQConstant.ORDER_PAYMENT_SUCCESS_ROUTING_KEY,
                message
        );
        log.info("支付成功消息发送成功");
    }

    public void sendOrderReminderMessage(OrderMessageDTO message) {
        log.info("发送催单消息, orderId: {}, orderNumber: {}",
                message.getOrderId(), message.getOrderNumber());
        rabbitTemplate.convertAndSend(
                WebSocketRabbitMQConstant.ORDER_NOTIFY_EXCHANGE,
                WebSocketRabbitMQConstant.ORDER_REMINDER_ROUTING_KEY,
                message
        );
        log.info("催单消息发送成功");
    }

    public void sendCartClearMessage(OrderCartClearMessage message) {
        log.info("发送清空购物车消息, userId: {}, orderNumber: {}",
                message.getUserId(), message.getOrderNumber());
        rabbitTemplate.convertAndSend(
                WebSocketRabbitMQConstant.ORDER_NOTIFY_EXCHANGE,
                com.sky.cart.constant.CartRabbitMQConstant.ORDER_CART_CLEAR_ROUTING_KEY,
                message
        );
        log.info("清空购物车消息发送成功");
    }
}
