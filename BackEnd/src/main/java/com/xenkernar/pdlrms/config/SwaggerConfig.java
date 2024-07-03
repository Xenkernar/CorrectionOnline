package com.xenkernar.pdlrms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI springShopOpenAPI() {

        return new OpenAPI()
                .components(components())
                // 2. 再在这里添加上Swagger要使用的安全策略
                // addList()中写上对应的key
                .addSecurityItem(new SecurityRequirement().addList("Authorization"));
    }
    // 1. 先在组件中注册安全策略
    private Components components(){
        return new Components()
                // 第一个参数是key值，后面是初始化一个安全策略的参数
                .addSecuritySchemes(
                        "Authorization",
                        new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("Authorization"));
    }

}