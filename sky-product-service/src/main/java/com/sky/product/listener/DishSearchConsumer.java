package com.sky.product.listener;

import com.rabbitmq.client.Channel;
import com.sky.product.constant.ProductRabbitMQConstant;
import com.sky.product.dto.DishSearchMessage;
import com.sky.product.service.DishSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class DishSearchConsumer {

    @Autowired
    private DishSyncService dishSyncService;

    @RabbitListener(queues = ProductRabbitMQConstant.DISH_SEARCH_SYNC_QUEUE)
    public void handleDishSearchSync(DishSearchMessage message, Channel channel, Message mqMessage) throws IOException {
        long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();

        try {
            log.info("接收到菜品索引同步消息: dishId={}, messageId={}",
                    message.getDishId(),
                    mqMessage.getMessageProperties().getMessageId());

            if (DishSearchMessage.OPERATION_SYNC.equals(message.getOperationType())) {
                dishSyncService.syncDishToMeilisearch(message.getDishId());
                log.info("菜品索引同步完成: dishId={}", message.getDishId());
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("处理菜品索引同步消息失败: dishId={}", message.getDishId(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    @RabbitListener(queues = ProductRabbitMQConstant.DISH_SEARCH_DELETE_QUEUE)
    public void handleDishSearchDelete(DishSearchMessage message, Channel channel, Message mqMessage) throws IOException {
        long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();

        try {
            log.info("接收到菜品索引删除消息: dishId={}, messageId={}",
                    message.getDishId(),
                    mqMessage.getMessageProperties().getMessageId());

            if (DishSearchMessage.OPERATION_DELETE.equals(message.getOperationType())) {
                dishSyncService.removeDishFromMeilisearch(message.getDishId());
                log.info("菜品索引删除完成: dishId={}", message.getDishId());
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("处理菜品索引删除消息失败: dishId={}", message.getDishId(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
