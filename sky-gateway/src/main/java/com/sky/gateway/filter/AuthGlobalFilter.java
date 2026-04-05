package com.sky.gateway.filter;

import com.sky.constant.JwtClaimsConstant;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Resource
    private JwtProperties jwtProperties;


    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/user/user/login",
            "/admin/employee/login",
            "/user/shop/status",
            "/ws/"
    );

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if(isExcludePath(path) || isWebSocketUpgrade(request)){
            return chain.filter(exchange);
        }

        String tokenName = isUserPath(path) ?
                jwtProperties.getUserTokenName():
                jwtProperties.getAdminTokenName();

        String secretKey = isUserPath(path) ?
                jwtProperties.getUserSecretKey() :
                jwtProperties.getAdminSecretKey();

        String token = request.getHeaders().getFirst(tokenName);
        if(token == null || token.isEmpty()){
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        try{
            Claims claims = JwtUtil.parseJWT(secretKey, token);

            Long currentId;
            String role;

            if(isUserPath(path)){
                currentId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
                role = "USER";
            } else {
                currentId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
                role = "ADMIN";
            }

            ServerHttpRequest.Builder builder = request.mutate();
            builder.header("X-Current-Id", String.valueOf(currentId));
            builder.header("X-User-Role", role);

            return chain.filter(exchange.mutate().request(builder.build()).build());

        }catch(Exception e){
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
    }

    private boolean isUserPath(String path) {
        return path.startsWith("/user/");
    }

    private boolean isExcludePath(String path) {
        return EXCLUDE_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isWebSocketUpgrade(ServerHttpRequest request) {
        String upgrade = request.getHeaders().getFirst("Upgrade");
        return "websocket".equalsIgnoreCase(upgrade);
    }
}
