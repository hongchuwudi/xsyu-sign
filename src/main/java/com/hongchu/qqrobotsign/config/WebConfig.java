package com.hongchu.qqrobotsign.config;

import com.hongchu.qqrobotsign.intercepter.RateLimitInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("🚀 注册限流拦截器...");
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns("/error", "/favicon.ico"); // 排除错误页面和图标
        log.info("✅ 限流拦截器注册成功");
    }
}