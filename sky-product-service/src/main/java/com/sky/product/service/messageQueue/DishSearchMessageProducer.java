package com.sky.product.service.messageQueue;

import com.sky.product.constant.ProductRabbitMQConstant;
import com.sky.product.dto.DishSearchMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DishSearchMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendSyncMessage(Long dishId) {
        log.info("发送菜品索引同步消息: dishId={}", dishId);
        DishSearchMessage message = DishSearchMessage.builder()
                .dishId(dishId)
                .operationType(DishSearchMessage.OPERATION_SYNC)
                .build();

        rabbitTemplate.convertAndSend(
                ProductRabbitMQConstant.PRODUCT_EXCHANGE,
                ProductRabbitMQConstant.DISH_SEARCH_SYNC_ROUTING_KEY,
                message
        );
        log.info("菜品索引同步消息发送成功: dishId={}", dishId);
    }

    public void sendDeleteMessage(Long dishId) {
        log.info("发送菜品索引删除消息: dishId={}", dishId);
        DishSearchMessage message = DishSearchMessage.builder()
                .dishId(dishId)
                .operationType(DishSearchMessage.OPERATION_DELETE)
                .build();

        rabbitTemplate.convertAndSend(
                ProductRabbitMQConstant.PRODUCT_EXCHANGE,
                ProductRabbitMQConstant.DISH_SEARCH_DELETE_ROUTING_KEY,
                message
        );
        log.info("菜品索引删除消息发送成功: dishId={}", dishId);
    }
}
