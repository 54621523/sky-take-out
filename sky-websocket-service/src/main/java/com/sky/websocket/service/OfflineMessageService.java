package com.sky.websocket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OfflineMessageService {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    private static final String OFFLINE_MESSAGE_KEY_PREFIX = "offline:message:";
    private static final long MESSAGE_EXPIRE_DAYS = 7;

    public void saveOfflineMessage(String role, String targetId, String message) {
        String key = OFFLINE_MESSAGE_KEY_PREFIX + role + ":" + targetId;
        try {
            redisTemplate.opsForList().rightPush(key, message);
            redisTemplate.expire(key, MESSAGE_EXPIRE_DAYS, TimeUnit.DAYS);
            log.info("离线消息保存成功 - role: {}, targetId: {}", role, targetId);
        } catch (Exception e) {
            log.error("保存离线消息失败 - role: {}, targetId: {}", role, targetId, e);
        }
    }

    public List<String> getOfflineMessages(String role, String targetId) {
        String key = OFFLINE_MESSAGE_KEY_PREFIX + role + ":" + targetId;
        try {
            List<Object> messages = redisTemplate.opsForList().range(key, 0, -1);
            if (messages != null && !messages.isEmpty()) {
                redisTemplate.delete(key);
                log.info("获取并清除离线消息 - role: {}, targetId: {}, 消息数: {}",
                        role, targetId, messages.size());
                List<String> result = new ArrayList<>();
                for (Object msg : messages) {
                    result.add(msg.toString());
                }
                return result;
            }
        } catch (Exception e) {
            log.error("获取离线消息失败 - role: {}, targetId: {}", role, targetId, e);
        }
        return new ArrayList<>();
    }

    public int getOfflineMessageCount(String role, String targetId) {
        String key = OFFLINE_MESSAGE_KEY_PREFIX + role + ":" + targetId;
        try {
            Long size = redisTemplate.opsForList().size(key);
            return size != null ? size.intValue() : 0;
        } catch (Exception e) {
            log.error("获取离线消息数量失败 - role: {}, targetId: {}", role, targetId, e);
            return 0;
        }
    }
}
