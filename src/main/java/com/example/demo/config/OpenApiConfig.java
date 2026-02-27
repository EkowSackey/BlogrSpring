package com.example.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearer-key",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .info(new Info()
                        .title("Blogr API")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Blogr Team")
                                .email("support@blogr.com")
                                .url("https://blogr.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org"))
                        .description("REST Endpoints for Blog Management. For Graph queries, use /graphiql."))
                .addSecurityItem(new SecurityRequirement().addList("bearer-key"));
    }

    @Bean
    public OperationCustomizer securityItemCustomizer() {
        return (operation, handlerMethod) -> {
            String methodName = handlerMethod.getMethod().getName();
            String className = handlerMethod.getBeanType().getSimpleName();
            String description = operation.getDescription() == null ? "" : operation.getDescription();

            // 1. Skip Public Endpoints
            if (methodName.equals("login") || methodName.equals("register")) {
                return operation;
            }

            // 2. Check for @PreAuthorize (Service or Controller level)
            Optional<PreAuthorize> preAuthorize = Optional.ofNullable(handlerMethod.getMethodAnnotation(PreAuthorize.class));
            if (preAuthorize.isEmpty()) {
                preAuthorize = Optional.ofNullable(handlerMethod.getBeanType().getAnnotation(PreAuthorize.class));
            }

            if (preAuthorize.isPresent()) {
                String roles = cleanRoleExpression(preAuthorize.get().value());
                operation.setDescription(description + "\n\n**Required Roles:** `" + roles + "`");
                return operation;
            }

            // 3. Fallback to Path-Based Security Mapping (matching SecurityConfig)
            if (className.equals("UserController") || className.equals("AnalyticsController")) {
                if (!methodName.equals("logout")) {
                    operation.setDescription(description + "\n\n**Required Roles:** `ADMIN` (via Filter Chain)");
                } else {
                    operation.setDescription(description + "\n\n**Required Roles:** `ANY AUTHENTICATED USER` (via Filter Chain)");
                }
            } else if (className.equals("PostController")) {
                if (handlerMethod.hasMethodAnnotation(GetMapping.class)) {
                    operation.setDescription(description + "\n\n**Required Roles:** `ADMIN`, `AUTHOR`, `READER` (via Filter Chain)");
                } else {
                    operation.setDescription(description + "\n\n**Required Roles:** `ADMIN`, `AUTHOR` (via Filter Chain)");
                }
            } else if (className.equals("CommentController")) {
                if (handlerMethod.hasMethodAnnotation(PostMapping.class)) {
                    operation.setDescription(description + "\n\n**Required Roles:** `ADMIN`, `AUTHOR`, `READER` (via Filter Chain)");
                } else {
                    operation.setDescription(description + "\n\n**Required Roles:** `ADMIN`, `AUTHOR` (via Filter Chain)");
                }
            }

            return operation;
        };
    }

    private String cleanRoleExpression(String roles) {
        return roles.replace("hasRole('", "")
                    .replace("hasAnyRole(", "")
                    .replace("')", "")
                    .replace(")", "")
                    .replace("'", "");
    }

    // This ensures SpringDoc doesn't try to document GraphQL's MappingHandlers
    static {
        SpringDocUtils.getConfig().addHiddenRestControllers(
                "org.springframework.graphql.data.method.annotation.support.DataFetcherHandlerMethod"
        );
    }
}
