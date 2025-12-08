package com.PBL6.Ecommerce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Configuration
 * Access Swagger UI at: https://localhost:8081/swagger-ui/index.html       
 * Access API Docs JSON at: https://localhost:8081/v3/api-docs
 */
@Configuration
public class SwaggerConfig {    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        // Define JWT Security Scheme
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("PBL6 E-Commerce API")
                        .version("1.0.0")
                        .description("RESTful API documentation for PBL6 E-Commerce Platform. " +
                                "Most endpoints require JWT Bearer token authentication. " +
                                "Login via /api/authenticate to get access token, then click 'Authorize' button and enter: Bearer <your-token>")
                        .contact(new Contact()
                                .name("PBL6 Team")
                                .email("support@pbl6ecommerce.com")
                                .url("https://github.com/ThanhThat12/PBL6_E-Commerce"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("https://localhost:" + serverPort)
                                .description("Local HTTPS Development Server"),
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local HTTP Development Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT Bearer token (without 'Bearer' prefix). Example: eyJhbGc...")
                        )
                );
    }

    /**
     * Group API để scan chỉ controllers, bỏ qua @ControllerAdvice
     * Tránh lỗi NoSuchMethodError với ControllerAdviceBean
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/**")
                .packagesToScan("com.PBL6.Ecommerce.controller")
                .build();
    }
}
