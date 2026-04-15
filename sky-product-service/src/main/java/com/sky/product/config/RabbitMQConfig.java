package com.sky.product.config;

import com.sky.product.constant.ProductRabbitMQConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setCreateMessageIds(true);
        return converter;
    }

    @Bean
    public TopicExchange productExchange() {
        return new TopicExchange(ProductRabbitMQConstant.PRODUCT_EXCHANGE, true, false);
    }

    @Bean
    public Queue dishSearchSyncQueue() {
        return QueueBuilder.durable(ProductRabbitMQConstant.DISH_SEARCH_SYNC_QUEUE).build();
    }

    @Bean
    public Queue dishSearchDeleteQueue() {
        return QueueBuilder.durable(ProductRabbitMQConstant.DISH_SEARCH_DELETE_QUEUE).build();
    }

    @Bean
    public Binding dishSearchSyncBinding(Queue dishSearchSyncQueue, TopicExchange productExchange) {
        return BindingBuilder.bind(dishSearchSyncQueue)
                .to(productExchange)
                .with(ProductRabbitMQConstant.DISH_SEARCH_SYNC_ROUTING_KEY);
    }

    @Bean
    public Binding dishSearchDeleteBinding(Queue dishSearchDeleteQueue, TopicExchange productExchange) {
        return BindingBuilder.bind(dishSearchDeleteQueue)
                .to(productExchange)
                .with(ProductRabbitMQConstant.DISH_SEARCH_DELETE_ROUTING_KEY);
    }
}
