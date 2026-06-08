package com.sky.product.listener;

import com.rabbitmq.client.Channel;
import com.sky.product.constant.ProductRabbitMQConstant;
import com.sky.product.dto.SetmealSearchMessage;
import com.sky.product.service.searchengine.SetmealSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class SetmealSearchConsumer {

    @Autowired
    private SetmealSyncService setmealSyncService;

    @RabbitListener(queues = ProductRabbitMQConstant.SETMEAL_SEARCH_SYNC_QUEUE)
    public void handleSetmealSearchSync(SetmealSearchMessage message, Channel channel, Message mqMessage) throws IOException {
        long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();

        try {
            log.info("接收到套餐索引同步消息: setmealId={}, messageId={}",
                    message.getSetmealId(),
                    mqMessage.getMessageProperties().getMessageId());

            if (SetmealSearchMessage.OPERATION_SYNC.equals(message.getOperationType())) {
                setmealSyncService.syncSetmealToMeilisearch(message.getSetmealId());
                log.info("套餐索引同步完成: setmealId={}", message.getSetmealId());
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("处理套餐索引同步消息失败: setmealId={}", message.getSetmealId(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    @RabbitListener(queues = ProductRabbitMQConstant.SETMEAL_SEARCH_DELETE_QUEUE)
    public void handleSetmealSearchDelete(SetmealSearchMessage message, Channel channel, Message mqMessage) throws IOException {
        long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();

        try {
            log.info("接收到套餐索引删除消息: setmealId={}, messageId={}",
                    message.getSetmealId(),
                    mqMessage.getMessageProperties().getMessageId());

            if (SetmealSearchMessage.OPERATION_DELETE.equals(message.getOperationType())) {
                setmealSyncService.removeSetmealFromMeilisearch(message.getSetmealId());
                log.info("套餐索引删除完成: setmealId={}", message.getSetmealId());
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("处理套餐索引删除消息失败: setmealId={}", message.getSetmealId(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
