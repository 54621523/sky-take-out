package com.sky.websocket.listener;

import com.alibaba.fastjson.JSONObject;

import com.sky.websocket.constant.WebSocketRabbitMQConstant;
import com.sky.websocket.service.OfflineMessageService;
import com.sky.websocket.websocket.WebSocketServer;
import com.rabbitmq.client.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CustomerServiceConsumer {

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private OfflineMessageService offlineMessageService;

    @Data
    public static class CustomerServiceMessage {
        private Long orderId;
        private String targetId;
        private String targetType;
        private String content;
    }

    @RabbitListener(queues = WebSocketRabbitMQConstant.ORDER_CUSTOMER_SERVICE_QUEUE)
    public void handleCustomerService(CustomerServiceMessage message, Channel channel, Message mqMessage) throws IOException {
        long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();

        try {
            log.info("接收到客服消息: {}", JSONObject.toJSONString(message));

            Map<String, Object> notifyMap = new HashMap<>();
            notifyMap.put("type", 4);
            notifyMap.put("orderid", message.getOrderId());
            notifyMap.put("content", message.getContent());
            notifyMap.put("timestamp", System.currentTimeMillis());

            String messageJson = JSONObject.toJSONString(notifyMap);

            if ("user".equals(message.getTargetType())) {
                if (webSocketServer.isUserOnline(message.getTargetId())) {
                    webSocketServer.sendToUser(message.getTargetId(), messageJson);
                } else {
                    offlineMessageService.saveOfflineMessage(WebSocketServer.ROLE_USER, message.getTargetId(), messageJson);
                }
            } else if ("shop".equals(message.getTargetType())) {
                if (webSocketServer.isShopOnline(message.getTargetId())) {
                    webSocketServer.sendToShop(message.getTargetId(), messageJson);
                } else {
                    offlineMessageService.saveOfflineMessage(WebSocketServer.ROLE_SHOP, message.getTargetId(), messageJson);
                }
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("处理客服消息失败", e);
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
