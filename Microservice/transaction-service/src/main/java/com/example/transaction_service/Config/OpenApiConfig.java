package com.example.transaction_service.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI Configuration for Swagger UI
 * 
 * This class configures the API documentation that appears in Swagger UI.
 * It provides metadata about your API (title, description, version) and
 * sets up JWT Bearer token authentication and custom headers for testing protected endpoints.
 */
@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Transaction Service API")
                                                .description("RESTful API for managing transactions. " +
                                                                "Supports transaction history, OTP verification, " +
                                                                "and statement generation with RabbitMQ integration. " +
                                                                "\n\n**Note:** Add X-User-Id, X-User-Role, and X-User-Email headers for authentication.")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("E-Wallet Team")
                                                                .email("support@ewallet.com")))
                                .components(new Components()
                                                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .description("Enter your JWT token obtained from User Service /auth/login endpoint"))
                                                // Add custom header parameters
                                                .addParameters("X-User-Id", new Parameter()
                                                                .in("header")
                                                                .name("X-User-Id")
                                                                .description("User ID (UUID) - Required for authentication")
                                                                .required(true)
                                                                .schema(new Schema<>().type("string").format("uuid")))
                                                .addParameters("X-User-Role", new Parameter()
                                                                .in("header")
                                                                .name("X-User-Role")
                                                                .description("User role (USER or ADMIN) - Required for authentication")
                                                                .required(true)
                                                                .schema(new Schema<>().type("string").example("USER")))
                                                .addParameters("X-User-Email", new Parameter()
                                                                .in("header")
                                                                .name("X-User-Email")
                                                                .description("User email - Required for some operations")
                                                                .required(false)
                                                                .schema(new Schema<>().type("string").format("email"))));
        }

        /**
         * Operation customizer to add custom headers to all endpoints
         */
        @Bean
        public OperationCustomizer operationCustomizer() {
                return (operation, handlerMethod) -> {
                        // Add X-User-Id header to all operations
                        operation.addParametersItem(new Parameter()
                                        .in("header")
                                        .name("X-User-Id")
                                        .description("User ID (UUID) - Required for authentication")
                                        .required(true)
                                        .schema(new Schema<>().type("string").format("uuid")));
                        
                        // Add X-User-Role header to all operations
                        operation.addParametersItem(new Parameter()
                                        .in("header")
                                        .name("X-User-Role")
                                        .description("User role (USER or ADMIN) - Required for authentication")
                                        .required(true)
                                        .schema(new Schema<>().type("string").example("USER")));
                        
                        // Add X-User-Email header to all operations (optional)
                        operation.addParametersItem(new Parameter()
                                        .in("header")
                                        .name("X-User-Email")
                                        .description("User email - Required for some operations")
                                        .required(false)
                                        .schema(new Schema<>().type("string").format("email")));
                        
                        return operation;
                };
        }
}

