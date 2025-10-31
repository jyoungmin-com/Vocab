package jyoungmin.vocabcommons.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

/**
 * Builder for creating OpenAPI configuration with JWT authentication
 */
public class OpenApiConfigBuilder {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";
    private static final String DEFAULT_VERSION = "v1.0.0";

    private OpenApiConfigBuilder() {
        // Utility class - prevent instantiation
    }

    /**
     * Build OpenAPI configuration with standard JWT authentication
     *
     * @param title       API title
     * @param description API description
     * @param serverUrl   Server URL (e.g., "http://localhost:8080")
     * @return Configured OpenAPI instance
     */
    public static OpenAPI build(String title, String description, String serverUrl) {
        return build(title, description, serverUrl, DEFAULT_VERSION);
    }

    /**
     * Build OpenAPI configuration with standard JWT authentication and custom version
     *
     * @param title       API title
     * @param description API description
     * @param serverUrl   Server URL (e.g., "http://localhost:8080")
     * @param version     API version
     * @return Configured OpenAPI instance
     */
    public static OpenAPI build(String title, String description, String serverUrl, String version) {
        return build(title, description, serverUrl, version, "Enter JWT token");
    }

    /**
     * Build OpenAPI configuration with standard JWT authentication and custom bearer description
     *
     * @param title             API title
     * @param description       API description
     * @param serverUrl         Server URL (e.g., "http://localhost:8080")
     * @param version           API version
     * @param bearerDescription Description for bearer token input
     * @return Configured OpenAPI instance
     */
    public static OpenAPI build(String title, String description, String serverUrl, String version, String bearerDescription) {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version))
                .servers(List.of(
                        new Server().url(serverUrl).description("Local Development Server")
                ))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(bearerDescription)))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
