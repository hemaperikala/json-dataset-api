package com.example.dataset.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI datasetOpenApi() {
        return new OpenAPI().info(new Info()
                .title("JSON Dataset API")
                .version("1.0.0")
                .description("Insert JSON records into named datasets and query them with group-by and sort-by operators."));
    }
}
