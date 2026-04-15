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
public class OrderReminderConsumer {

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private OfflineMessageService offlineMessageService;

    @Data
    public static class ReminderMessage {
        private Long orderId;
        private String orderNumber;
        private Long userId;
        private String content;
    }

    @RabbitListener(queues = WebSocketRabbitMQConstant.ORDER_REMINDER_QUEUE)
    public void handleOrderReminder(ReminderMessage message, Channel channel, Message mqMessage) throws IOException {
        long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();

        try {
            log.info("接收到催单消息: {}", JSONObject.toJSONString(message));

            Map<String, Object> notifyMap = new HashMap<>();
            notifyMap.put("type", 2);
            notifyMap.put("orderid", message.getOrderId());
            notifyMap.put("content", "用户催单: " + message.getOrderNumber());
            notifyMap.put("timestamp", System.currentTimeMillis());

            String messageJson = JSONObject.toJSONString(notifyMap);
            String shopId = "DEFAULT_SHOP";

            if (webSocketServer.isShopOnline(shopId)) {
                webSocketServer.sendToShop(shopId, messageJson);
                log.info("催单通知已发送给商家 - shopId: {}, orderId: {}", shopId, message.getOrderId());
            } else {
                offlineMessageService.saveOfflineMessage(WebSocketServer.ROLE_SHOP, shopId, messageJson);
                log.info("商家不在线，催单消息已保存为离线消息 - shopId: {}, orderId: {}", shopId, message.getOrderId());
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("处理催单消息失败, orderId: {}", message.getOrderId(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
