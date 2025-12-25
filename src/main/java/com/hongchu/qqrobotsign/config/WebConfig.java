package com.hongchu.qqrobotsign.config;

import com.hongchu.qqrobotsign.intercepter.JwtInterceptor;
import com.hongchu.qqrobotsign.intercepter.RateLimitInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Autowired private RateLimitInterceptor rateLimitInterceptor;
    @Autowired private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("🚀 注册限流拦截器...");
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/error",
                        "/favicon.ico",
                        // 静态资源路径
                        "/static/**",
                        "/public/**",
                        "/resources/**",
                        // 前端页面
                        "/index.html",
                        "/",
                        // 前端资源
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/fonts/**"
                )
                .order(1);
        log.info("✅ 限流拦截器注册成功");

        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/index.html",
                        "/",
                        "/test/**",
                        "/user/login",
                        "/sign/all-admin/**",
                        "/error",
                        "/favicon.ico",
                        // 静态资源路径
                        "/static/**",
                        "/public/**",
                        "/resources/**",
                        // 前端资源
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/fonts/**",
                        // 开放API文档
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/webjars/**"
                )
                .order(2);
        log.info("✅ JWT拦截器注册成功");
    }
}