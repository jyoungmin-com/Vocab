package jyoungmin.vocabcommons.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * Helper class for common Spring Security configurations.
 * Provides reusable configuration methods for JWT-based REST APIs.
 * Used by VocabAuth and VocabList services to reduce configuration duplication.
 */
public class SecurityConfigHelper {

    private SecurityConfigHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Configures common security settings for JWT-based REST APIs.
     * - Disables CSRF protection (not needed for stateless JWT auth)
     * - Sets session policy to STATELESS
     * - Disables form login and HTTP basic auth
     *
     * @param http HttpSecurity instance
     * @return HttpSecurity instance for method chaining
     * @throws Exception if configuration fails
     */
    public static HttpSecurity configureStatelessJwtSecurity(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);
    }

    /**
     * Configures Swagger/OpenAPI endpoints to be publicly accessible.
     *
     * @param http HttpSecurity instance
     * @return HttpSecurity instance for method chaining
     * @throws Exception if configuration fails
     */
    public static HttpSecurity configureSwaggerPublicAccess(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                );
    }
}
