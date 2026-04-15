package com.sky.websocket.config;

import com.sky.websocket.constant.WebSocketRabbitMQConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    public TopicExchange orderExchange() {
        return new TopicExchange(WebSocketRabbitMQConstant.ORDER_NOTIFY_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderPaymentSuccessQueue() {
        return QueueBuilder.durable(WebSocketRabbitMQConstant.ORDER_PAYMENT_SUCCESS_QUEUE).build();
    }

    @Bean
    public Queue orderReminderQueue() {
        return QueueBuilder.durable(WebSocketRabbitMQConstant.ORDER_REMINDER_QUEUE).build();
    }

    @Bean
    public Queue orderCustomerServiceQueue() {
        return QueueBuilder.durable(WebSocketRabbitMQConstant.ORDER_CUSTOMER_SERVICE_QUEUE).build();
    }


    @Bean
    public Binding orderPaymentSuccessBinding(Queue orderPaymentSuccessQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderPaymentSuccessQueue)
                .to(orderExchange)
                .with(WebSocketRabbitMQConstant.ORDER_PAYMENT_SUCCESS_ROUTING_KEY);
    }

    @Bean
    public Binding orderReminderBinding(Queue orderReminderQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderReminderQueue)
                .to(orderExchange)
                .with(WebSocketRabbitMQConstant.ORDER_REMINDER_ROUTING_KEY);
    }


    @Bean
    public Binding orderCustomerServiceBinding(Queue orderCustomerServiceQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderCustomerServiceQueue)
                .to(orderExchange)
                .with(WebSocketRabbitMQConstant.ORDER_CUSTOMER_SERVICE_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
