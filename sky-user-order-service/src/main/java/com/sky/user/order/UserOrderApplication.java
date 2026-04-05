package com.sky.user.order;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient
@ComponentScan({"com.sky.user.order", "com.sky.config", "com.sky.interceptor"})
public class UserOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserOrderApplication.class, args);
    }

}
