package com.sky.admin.operation;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableDubbo
@EnableDiscoveryClient
@MapperScan("com.sky.admin.operation.mapper")
@ComponentScan("com.sky")
class AdminOperationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminOperationApplication.class, args);
    }

}
