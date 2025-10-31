package jyoungmin.vocabauth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS (Cross-Origin Resource Sharing) configuration for the authentication service.
 * Allows the frontend application to make cross-origin requests with credentials.
 */
@Configuration
public class CorsConfig {

    /**
     * Frontend application URL allowed to access this API
     */
    @Value("${server.frontend.url}")
    private String frontendUrl;

    /**
     * Configures CORS filter with credentials support and frontend origin access.
     * Allows all headers and methods from the configured frontend URL.
     *
     * @return configured CORS filter
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration configuration = new CorsConfiguration();

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        configuration.addAllowedOrigin(frontendUrl);

        // Allow all headers and HTTP methods
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");

        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", configuration);
        return new CorsFilter(source);
    }
}
