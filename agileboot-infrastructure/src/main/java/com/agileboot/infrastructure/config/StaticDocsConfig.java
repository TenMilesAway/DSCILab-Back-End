package com.agileboot.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 将项目根目录下的“实验室用户管理API完整文档.md”通过 HTTP 暴露，
 * 便于在 Swagger/Knife4j 的 External Docs 中直接访问。
 */
@Configuration
public class StaticDocsConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String baseDir = System.getProperty("user.dir");
        // 直接映射根目录下的 Markdown 文档
        registry.addResourceHandler("/实验室用户管理API完整文档.md")
                .addResourceLocations("file:" + baseDir + "/实验室用户管理API完整文档.md");

        // 也预留 /docs/** 到 classpath:/static/docs/ 的映射，便于将来迁移到类路径下
        registry.addResourceHandler("/docs/**")
                .addResourceLocations("classpath:/static/docs/");
    }
}

