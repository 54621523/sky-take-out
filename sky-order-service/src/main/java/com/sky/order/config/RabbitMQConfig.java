package com.sky.order.config;


import com.sky.order.constant.OrderRabbitMQConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setCreateMessageIds(true);
        return converter;
    }
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(OrderRabbitMQConstant.ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderTimeoutQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 15 * 60 * 1000);
        args.put("x-dead-letter-exchange", OrderRabbitMQConstant.ORDER_EXCHANGE);
        args.put("x-dead-letter-routing-key", OrderRabbitMQConstant.ORDER_TIMEOUT_DLX_ROUTING_KEY);
        return QueueBuilder.durable(OrderRabbitMQConstant.ORDER_TIMEOUT_QUEUE)
                .withArguments(args)
                .build();
    }

    @Bean
    public Queue orderTimeoutDlxQueue() {
        return QueueBuilder.durable(OrderRabbitMQConstant.ORDER_TIMEOUT_DLX_QUEUE).build();
    }

    @Bean
    public Binding orderTimeoutDelayBinding(Queue orderTimeoutQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderTimeoutQueue)
                .to(orderExchange)
                .with(OrderRabbitMQConstant.ORDER_TIMEOUT_ROUTING_KEY);
    }

    @Bean
    public Binding orderTimeoutDlxBinding(Queue orderTimeoutDlxQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderTimeoutDlxQueue)
                .to(orderExchange)
                .with(OrderRabbitMQConstant.ORDER_TIMEOUT_DLX_ROUTING_KEY);
    }
}
