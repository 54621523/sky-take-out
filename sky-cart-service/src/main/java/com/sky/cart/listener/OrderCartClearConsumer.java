package com.sky.cart.listener;

import com.rabbitmq.client.Channel;
import com.sky.cart.constant.CartRabbitMQConstant;
import com.sky.cart.domain.po.ShoppingCart;
import com.sky.cart.mapper.ShoppingCartMapper;
import com.sky.order.dto.OrderCartClearMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OrderCartClearConsumer {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @RabbitListener(queues = CartRabbitMQConstant.ORDER_CART_CLEAR_QUEUE)
    public void handleCartClear(OrderCartClearMessage message, Channel channel, Message mqMessage) throws IOException {
        long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();

        try {
            log.info("接收到清空购物车消息: userId={}, orderNumber={}, messageId={}",
                    message.getUserId(),
                    message.getOrderNumber(),
                    mqMessage.getMessageProperties().getMessageId());

            shoppingCartMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ShoppingCart>()
                    .eq(ShoppingCart::getUserId, message.getUserId()));

            log.info("购物车清空完成: userId={}", message.getUserId());
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("处理清空购物车消息失败: userId={}", message.getUserId(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
