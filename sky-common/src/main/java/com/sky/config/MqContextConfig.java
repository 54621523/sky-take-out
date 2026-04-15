package com.sky.config;

import com.sky.filter.MQContextOutboundFilter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqContextConfig {

    @Bean
    public BeanPostProcessor rabbitTemplatePostProcessor(MQContextOutboundFilter mqContextOutboundFilter) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof RabbitTemplate) {
                    RabbitTemplate rabbitTemplate = (RabbitTemplate) bean;
                    rabbitTemplate.setBeforePublishPostProcessors(mqContextOutboundFilter);
                }
                return bean;
            }
        };
    }
}
