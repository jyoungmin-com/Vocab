package jyoungmin.vocabauth.config;

import io.swagger.v3.oas.models.OpenAPI;
import jyoungmin.vocabcommons.config.OpenApiConfigBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for the authentication service.
 * Provides interactive API documentation
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures OpenAPI documentation for the authentication API.
     *
     * @return configured OpenAPI specification
     */
    @Bean
    public OpenAPI vocabAuthOpenAPI() {
        return OpenApiConfigBuilder.build(
                "Vocab Auth API",
                "Authentication and User Management API for Vocab Application",
                "http://localhost:8080"
        );
    }
}
