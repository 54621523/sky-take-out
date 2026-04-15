package com.sky.filter;

import com.sky.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MQContextOutboundFilter implements MessagePostProcessor {

    private static final String USER_ID_HEADER = "X-Current-Id";

    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        Long currentId = BaseContext.getCurrentId();
        if (currentId != null) {
            message.getMessageProperties().setHeader(USER_ID_HEADER, currentId);
            log.debug("MQ消息设置用户上下文 - currentId: {}", currentId);
        }
        return message;
    }
}
