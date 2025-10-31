package com.altong.altong_backend.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI altongOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Altong API")
                        .description("알통 통합 로그인/회원가입 API 명세서")
                        .version("v1.0.0"));
    }
}