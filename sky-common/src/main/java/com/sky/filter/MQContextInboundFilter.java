package com.sky.filter;

import com.sky.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class MQContextInboundFilter {

    private static final String USER_ID_HEADER = "X-Current-Id";

    @Around("@annotation(org.springframework.amqp.rabbit.annotation.RabbitListener)")
    public Object aroundRabbitListener(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        for (Object arg : args) {
            if (arg instanceof Message) {
                Message message = (Message) arg;
                Object userId = message.getMessageProperties().getHeaders().get(USER_ID_HEADER);
                if (userId != null) {
                    BaseContext.setCurrentId(Long.valueOf(userId.toString()));
                    log.debug("MQ消费者恢复用户上下文 - currentId: {}", userId);
                }
                break;
            }
        }

        try {
            return joinPoint.proceed();
        } finally {
            BaseContext.removeCurrentId();
            log.debug("MQ消费者清理用户上下文");
        }
    }
}
