package com.sky.config;

import com.sky.interceptor.InfoInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private InfoInterceptor InfoInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(InfoInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/user/user/login", "/admin/employee/login");
    }
}
