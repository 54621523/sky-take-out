package com.sky.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_KEY = "requestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        MDC.put(REQUEST_ID_KEY, requestId);

        String path = request.getURI().getPath();
        String method = request.getMethodValue();
        String queryParams = request.getURI().getQuery();
        String clientIp = getClientIp(request);

        boolean isWebSocket = isWebSocketUpgrade(request);

        if (isWebSocket) {
            log.info(">>> WebSocket 连接请求 | {} {} | IP: {}", method, path, clientIp);
        } else {
            log.info(">>> 请求开始 | {} {} | IP: {} | 参数: {}",
                    method, path, clientIp, queryParams);
        }

        LocalDateTime startTime = LocalDateTime.now();

        return chain.filter(exchange)
                .contextWrite(context -> context.put(REQUEST_ID_KEY, requestId))
                .doOnSuccess(v -> {
                    long duration = Duration.between(startTime, LocalDateTime.now()).toMillis();
                    MDC.put(REQUEST_ID_KEY, requestId);

                    if (isWebSocket) {
                        log.info("<<< WebSocket 连接建立成功 | 耗时: {}ms", duration);
                    } else {
                        log.info("<<< 请求结束 | 状态: {} | 耗时: {}ms",
                                exchange.getResponse().getStatusCode(), duration);
                    }
                    MDC.clear();
                })
                .doOnError(error -> {
                    long duration = Duration.between(startTime, LocalDateTime.now()).toMillis();
                    MDC.put(REQUEST_ID_KEY, requestId);

                    if (isWebSocket) {
                        log.error("!!! WebSocket 连接失败 | 耗时: {}ms | 错误: {}",
                                duration, error.getMessage(), error);
                    } else {
                        log.error("!!! 请求异常 | 耗时: {}ms | 错误: {}",
                                duration, error.getMessage(), error);
                    }
                    MDC.clear();
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress() != null
                    ? request.getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private boolean isWebSocketUpgrade(ServerHttpRequest request) {
        String upgrade = request.getHeaders().getFirst("Upgrade");
        return "websocket".equalsIgnoreCase(upgrade);
    }
}
