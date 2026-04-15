package com.sky.order.listener;

import com.sky.order.constant.OrderRabbitMQConstant;
import com.sky.order.domain.po.Orders;
import com.sky.order.mapper.OrderMapper;
import com.rabbitmq.client.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
public class OrderTimeoutConsumer {

    @Autowired
    private OrderMapper orderMapper;

    @Data
    public static class TimeoutMessage {
        private Long orderId;
        private String orderNumber;
        private Long userId;
    }

    @RabbitListener(queues = OrderRabbitMQConstant.ORDER_TIMEOUT_DLX_QUEUE)
    public void handleOrderTimeout(TimeoutMessage message, Channel channel, Message mqMessage) throws IOException {
        long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();

        try {
            log.info("接收到订单超时消息: orderId={}, orderNumber={}",
                    message.getOrderId(), message.getOrderNumber());

            Orders order = orderMapper.selectById(message.getOrderId());

            if (order == null) {
                log.warn("订单不存在，忽略超时消息: orderId={}", message.getOrderId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (Orders.PAID.equals(order.getPayStatus())) {
                log.info("订单已支付，忽略超时消息: orderId={}", message.getOrderId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (!Orders.PENDING_PAYMENT.equals(order.getStatus())) {
                log.info("订单状态已变更，忽略超时消息: orderId={}, status={}",
                        message.getOrderId(), order.getStatus());
                channel.basicAck(deliveryTag, false);
                return;
            }

            orderMapper.updateById(Orders.builder()
                    .id(order.getId())
                    .status(Orders.CANCELLED)
                    .cancelReason("订单支付超时")
                    .cancelTime(LocalDateTime.now())
                    .build());

            log.info("订单超时取消成功: orderId={}", message.getOrderId());

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("处理订单超时消息失败: orderId={}", message.getOrderId(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
