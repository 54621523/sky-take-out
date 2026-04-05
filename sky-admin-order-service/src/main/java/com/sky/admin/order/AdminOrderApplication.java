package com.sky.admin.order;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableDubbo
@ComponentScan("com.sky")
class AdminOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminOrderApplication.class, args);
    }

}
