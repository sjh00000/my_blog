package com.blog.sun.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 解决跨域问题
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 配置CORS(Cross-Origin Resource Sharing)允许的跨域请求。
     * 此方法用于添加对所有API路径的CORS配置，以允许来自任何来源的请求。
     * 它定义了跨域请求的各种权限，如允许的方法、头部和凭证等。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 添加对所有路径的CORS映射
        registry.addMapping("/**")
                // 允许来自任何来源的请求
                .allowedOrigins("*")
                // 允许请求使用的方法
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                // 不允许请求携带凭证（如cookies）
                .allowCredentials(false)
                // 设置预检请求的缓存时间，单位为秒
                .maxAge(3600)
                // 允许所有请求头部
                .allowedHeaders("*");
    }

}