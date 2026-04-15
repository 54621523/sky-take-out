package com.sky.cart.config;

import com.sky.cart.constant.CartRabbitMQConstant;
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
    public TopicExchange orderNotifyExchange() {
        return new TopicExchange(CartRabbitMQConstant.ORDER_NOTIFY_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderCartClearQueue() {
        return QueueBuilder.durable(CartRabbitMQConstant.ORDER_CART_CLEAR_QUEUE).build();
    }

    @Bean
    public Binding orderCartClearBinding(Queue orderCartClearQueue, TopicExchange orderNotifyExchange) {
        return BindingBuilder.bind(orderCartClearQueue)
                .to(orderNotifyExchange)
                .with(CartRabbitMQConstant.ORDER_CART_CLEAR_ROUTING_KEY);
    }
}
