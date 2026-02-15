package com.example.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                        .title("Post Service API")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Blogr Team")
                                .email("support@blogr.com")
                                .url("https://blogr.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org"))
                        .description("REST Endpoints for Post Management. For Graph queries, use /graphiql."))
                .addSecurityItem(new SecurityRequirement().addList("bearer-key"));
    }

    // This ensures SpringDoc doesn't try to document GraphQL's MappingHandlers
    static {
        SpringDocUtils.getConfig().addHiddenRestControllers(
                "org.springframework.graphql.data.method.annotation.support.DataFetcherHandlerMethod"
        );
    }
}
