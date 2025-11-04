package com.altong.altong_backend.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;

@OpenAPIDefinition(
    info = @Info(
        title = "Altong API",
        version = "v1.0.0",
        description = "알통 통합 로그인/회원가입 API 명세서"
    ),
    security = {
        @SecurityRequirement(name = "bearerAuth")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)

public class SwaggerConfig {

    @Bean
    public OpenAPI altongOpenAPI() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new io.swagger.v3.oas.models.security.SecurityScheme()
                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
            // 전역 SecurityRequirement(모델 클래스)도 풀네임으로
            .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("bearerAuth"))
            .info(new io.swagger.v3.oas.models.info.Info()
                .title("Altong API")
                .version("v1.0.0")
                .description("알통 로그인/회원가입 및 인증 테스트용 Swagger 문서"));
    }
}