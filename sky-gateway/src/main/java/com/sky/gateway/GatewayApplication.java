package com.sky.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.sky", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.sky\\.config\\..*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.sky\\.interceptor\\..*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.sky\\.filter\\..*")
})
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
