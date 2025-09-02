package com.agileboot.infrastructure.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author valarchie
 * SpringDoc API文档相关配置
 */
@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI agileBootApi() {
        return new OpenAPI()
            .info(new Info().title("Agileboot后台管理系统")
                .description("登录 /login 现同时支持 sys_user 与 lab_user：按用户名先查 sys_user，若无再查 lab_user；lab_user.identity=1 视为管理员，全权限；否则为只读实验室权限。")
                .version("v1.8.0")
                .license(new License().name("MIT 3.0").url("https://github.com/valarchie/AgileBoot-Back-End")))
            .externalDocs(new ExternalDocumentation()
                .description("实验室用户管理API完整文档（Markdown）")
                .url("/实验室用户管理API完整文档.md"));
    }

}
