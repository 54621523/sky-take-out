package com.sky.product.service;

import com.sky.product.constant.ProductRabbitMQConstant;
import com.sky.product.dto.SetmealSearchMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SetmealSearchMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendSyncMessage(Long setmealId) {
        log.info("发送套餐索引同步消息: setmealId={}", setmealId);
        SetmealSearchMessage message = SetmealSearchMessage.builder()
                .setmealId(setmealId)
                .operationType(SetmealSearchMessage.OPERATION_SYNC)
                .build();

        rabbitTemplate.convertAndSend(
                ProductRabbitMQConstant.PRODUCT_EXCHANGE,
                ProductRabbitMQConstant.SETMEAL_SEARCH_SYNC_ROUTING_KEY,
                message
        );
        log.info("套餐索引同步消息发送成功: setmealId={}", setmealId);
    }

    public void sendDeleteMessage(Long setmealId) {
        log.info("发送套餐索引删除消息: setmealId={}", setmealId);
        SetmealSearchMessage message = SetmealSearchMessage.builder()
                .setmealId(setmealId)
                .operationType(SetmealSearchMessage.OPERATION_DELETE)
                .build();

        rabbitTemplate.convertAndSend(
                ProductRabbitMQConstant.PRODUCT_EXCHANGE,
                ProductRabbitMQConstant.SETMEAL_SEARCH_DELETE_ROUTING_KEY,
                message
        );
        log.info("套餐索引删除消息发送成功: setmealId={}", setmealId);
    }
}
