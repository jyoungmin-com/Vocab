package jyoungmin.vocablist.config;

import io.swagger.v3.oas.models.OpenAPI;
import jyoungmin.vocabcommons.config.OpenApiConfigBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for the vocabulary list service.
 * Provides interactive API documentation
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures OpenAPI documentation for the vocabulary list API.
     *
     * @return configured OpenAPI specification
     */
    @Bean
    public OpenAPI vocabListOpenAPI() {
        return OpenApiConfigBuilder.build(
                "Vocab List API",
                "Vocabulary and List Management API for Vocab Application",
                "http://localhost:8081",
                "v1.0.0",
                "Enter JWT token from Auth Service"
        );
    }
}
