package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Post Service API")
                        .version("1.0")
                        .description("REST Endpoints for Post Management. For Graph queries, use /graphiql."));
    }

    // This ensures SpringDoc doesn't try to document GraphQL's MappingHandlers
    static {
        SpringDocUtils.getConfig().addHiddenRestControllers(
                "org.springframework.graphql.data.method.annotation.support.DataFetcherHandlerMethod"
        );
    }
}
