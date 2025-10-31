package jyoungmin.vocabauth.security;

import jyoungmin.vocabcommons.security.JwtAuthenticationEntryPoint;
import jyoungmin.vocabcommons.security.SecurityConfigHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the authentication service.
 * Configures JWT-based stateless authentication with custom filters and security rules.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * Configures the authentication manager for handling user authentication.
     *
     * @param authenticationConfiguration Spring's authentication configuration
     * @return configured authentication manager
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Provides a password encoder for encrypting user passwords.
     * Uses BCrypt hashing algorithm for secure password storage.
     *
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the security filter chain with JWT authentication.
     * Sets up stateless session management, disables CSRF, and defines access rules.
     *
     * @param httpSecurity          the HTTP security configuration
     * @param authenticationManager the authentication manager
     * @return configured security filter chain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, AuthenticationManager authenticationManager) throws Exception {
        // Configure common stateless JWT security settings
        SecurityConfigHelper.configureStatelessJwtSecurity(httpSecurity);

        // Configure Swagger public access
        SecurityConfigHelper.configureSwaggerPublicAccess(httpSecurity);

        httpSecurity
                // Add JWT authentication filter before standard username/password filter
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)

                // Custom authentication entry point for handling auth failures
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // Define access rules for auth endpoints
                .authorizeHttpRequests(authorize -> authorize
                        // Public auth endpoints
                        .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/duplicate/**", "/api/v1/auth/refresh").permitAll()
                        // Protected auth endpoints
                        .requestMatchers("/api/v1/auth/logout", "/api/v1/auth/me").authenticated()
                        // All other API calls require authentication
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll());

        return httpSecurity.build();
    }
}
