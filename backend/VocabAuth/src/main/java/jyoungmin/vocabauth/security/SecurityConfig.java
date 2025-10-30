package jyoungmin.vocabauth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
// Spring Security를 활성화
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfig corsConfig;
    private final JwtTokenProvider jwtTokenProvider;

    //AuthenticationManager 빈을 생성하는 메소드
    //스프링 시큐리티의 인증을 담당하는 매니저를 설정
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    //비밀번호 암호화를 위한 인코더를 빈으로 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, AuthenticationManager authenticationManager) throws Exception{
        httpSecurity
//                .addFilter(corsConfig.corsFilter())
//                // JWT auth filter
//                .addFilter(new JwtAuthenticationFilter(authenticationManager, ))

                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)

                // CSRF 보호 비활성화 (JWT 사용으로 불필요) 왜지?
                // JWT를 사용하는 REST API에서는 CSRF 공격 방지가 불필요
                // 토큰 기반 인증이 CSRF 공격을 방지할 수 있기 때문
                .csrf(AbstractHttpConfigurer::disable)


                // 세션 설정 (JWT는 세션을 사용하지 않음)
                // JWT는 상태를 저장하지 않는(stateless) 방식이므로 세션이 불필요
                // 서버의 확장성과 성능 향상을 위해 세션을 사용하지 않음
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 폼 로그인 비활성화
                // REST API에서는 폼 로그인 방식을 사용하지 않음
                // JWT 토큰 기반의 인증을 사용하므로 불필요
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증 비활성화
                // 기본 인증은 보안에 취약하고 JWT를 사용하므로 불필요
                // 매 요청마다 인증 정보를 보내는 방식이라 보안에 취약
                .httpBasic(AbstractHttpConfigurer::disable)


                .authorizeHttpRequests(authorize -> authorize
                        // Swagger UI and OpenAPI docs
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
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
