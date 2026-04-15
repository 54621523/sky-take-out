package com.sky.websocket.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/{role}/{sid}")
@Slf4j
public class WebSocketServer {

    public static final String ROLE_USER = "user";
    public static final String ROLE_SHOP = "shop";
    public static final String ROLE_ADMIN = "admin";

    private static final Map<String, Session> userSessionMap = new ConcurrentHashMap<>();
    private static final Map<String, Session> shopSessionMap = new ConcurrentHashMap<>();
    private static final Map<String, Session> adminSessionMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("role") String role, @PathParam("sid") String sid) {
        switch (role) {
            case ROLE_USER:
                userSessionMap.put(sid, session);
                log.info("用户WebSocket连接建立 - userId: {}, 当前用户连接数: {}", sid, userSessionMap.size());
                break;
            case ROLE_SHOP:
                shopSessionMap.put(sid, session);
                log.info("商家WebSocket连接建立 - shopId: {}, 当前商家连接数: {}", sid, shopSessionMap.size());
                break;
            case ROLE_ADMIN:
                adminSessionMap.put(sid, session);
                log.info("管理员WebSocket连接建立 - adminId: {}, 当前管理员连接数: {}", sid, adminSessionMap.size());
                break;
            default:
                log.warn("未知的WebSocket角色: {}", role);
                try {
                    session.close();
                } catch (IOException e) {
                    log.error("关闭异常会话失败", e);
                }
        }
    }

    @OnClose
    public void onClose(@PathParam("role") String role, @PathParam("sid") String sid) {
        switch (role) {
            case ROLE_USER:
                userSessionMap.remove(sid);
                log.info("用户WebSocket连接关闭 - userId: {}, 当前用户连接数: {}", sid, userSessionMap.size());
                break;
            case ROLE_SHOP:
                shopSessionMap.remove(sid);
                log.info("商家WebSocket连接关闭 - shopId: {}, 当前商家连接数: {}", sid, shopSessionMap.size());
                break;
            case ROLE_ADMIN:
                adminSessionMap.remove(sid);
                log.info("管理员WebSocket连接关闭 - adminId: {}, 当前管理员连接数: {}", sid, adminSessionMap.size());
                break;
        }
    }

    @OnError
    public void onError(Session session, Throwable error, @PathParam("role") String role, @PathParam("sid") String sid) {
        log.error("WebSocket错误 - role: {}, sid: {}", role, sid, error);
        onClose(role, sid);
    }

    public void sendToUser(String userId, String message) {
        Session session = userSessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                log.debug("消息发送给用户成功 - userId: {}", userId);
            } catch (IOException e) {
                log.error("发送消息给用户失败 - userId: {}", userId, e);
                userSessionMap.remove(userId);
            }
        } else {
            log.warn("用户不在线或会话已关闭 - userId: {}", userId);
        }
    }

    public void sendToShop(String shopId, String message) {
        Session session = shopSessionMap.get(shopId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                log.debug("消息发送给商家成功 - shopId: {}", shopId);
            } catch (IOException e) {
                log.error("发送消息给商家失败 - shopId: {}", shopId, e);
                shopSessionMap.remove(shopId);
            }
        } else {
            log.warn("商家不在线或会话已关闭 - shopId: {}", shopId);
        }
    }

    public void sendToAdmin(String adminId, String message) {
        Session session = adminSessionMap.get(adminId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                log.debug("消息发送给管理员成功 - adminId: {}", adminId);
            } catch (IOException e) {
                log.error("发送消息给管理员失败 - adminId: {}", adminId, e);
                adminSessionMap.remove(adminId);
            }
        } else {
            log.warn("管理员不在线或会话已关闭 - adminId: {}", adminId);
        }
    }

    public boolean isUserOnline(String userId) {
        Session session = userSessionMap.get(userId);
        return session != null && session.isOpen();
    }

    public boolean isShopOnline(String shopId) {
        Session session = shopSessionMap.get(shopId);
        return session != null && session.isOpen();
    }

    public boolean isAdminOnline(String adminId) {
        Session session = adminSessionMap.get(adminId);
        return session != null && session.isOpen();
    }

    public int getUserOnlineCount() {
        return userSessionMap.size();
    }

    public int getShopOnlineCount() {
        return shopSessionMap.size();
    }

    public int getAdminOnlineCount() {
        return adminSessionMap.size();
    }
}
