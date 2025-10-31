package jyoungmin.vocablist.config;

import jyoungmin.vocabcommons.security.JwtAuthenticationEntryPoint;
import jyoungmin.vocabcommons.security.SecurityConfigHelper;
import jyoungmin.vocablist.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the vocabulary list service.
 * Configures JWT-based authentication and authorization rules.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * JWT authentication filter for token validation
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Custom entry point for authentication failures
     */
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * Configures the security filter chain with JWT authentication.
     *
     * @param http the HTTP security configuration
     * @return configured security filter chain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Configure common stateless JWT security settings
        SecurityConfigHelper.configureStatelessJwtSecurity(http);

        // Configure Swagger public access
        SecurityConfigHelper.configureSwaggerPublicAccess(http);

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").authenticated()  // All API paths require authentication
                        .anyRequest().permitAll()  // Allow all other requests
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // Custom response on authentication failure
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
