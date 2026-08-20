package com.p191.telemetry.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Khai báo 2 cơ chế xác thực cho Swagger UI:
 *  - deviceApiKey: header X-Api-Key (luồng ghi của app khách)
 *  - bearerAuth:   JWT Bearer (luồng đọc của admin)
 * Nút "Authorize" trên Swagger UI sẽ cho nhập cả hai; mỗi endpoint tự dùng cái đúng
 * nhờ @SecurityRequirement trên controller.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI p191OpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("P-191 Telemetry API")
                        .version("0.0.1")
                        .description("Device ghi (X-Api-Key) · Admin đọc (JWT role ADMIN)"))
                .components(new Components()
                        .addSecuritySchemes("deviceApiKey", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Api-Key"))
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
