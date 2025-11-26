package com.example.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI Configuration for Swagger UI
 * 
 * This class configures the API documentation that appears in Swagger UI.
 * It provides metadata about your API (title, description, version) and
 * sets up JWT Bearer token authentication for testing protected endpoints.
 */
@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("User Service API")
                                                .description("RESTful API for managing users. " +
                                                                "Supports user registration, authentication with JWT tokens, "
                                                                +
                                                                "and user management.")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("E-Wallet Team")
                                                                .email("support@ewallet.com")))
                                .components(new Components()
                                                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .description("Enter your JWT token obtained from /auth/login endpoint")))
                                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
        }
}
