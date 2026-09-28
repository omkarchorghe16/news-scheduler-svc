package com.stocknews.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI stockNewsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Stock News Scheduler API")
                .version("1.0.0")
                .description("Stock news digest and multi-provider stock profile API"));
    }
}
