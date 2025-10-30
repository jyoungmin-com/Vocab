package jyoungmin.vocabauth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Value("${server.frontend.url}")
    private String frontendUrl;

    @Bean
    public CorsFilter corsFilter() {
        // CORS 설정을 URL 패턴별로 적용할 수 있게 해주는 클래스
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowCredentials(true);
        configuration.addAllowedOrigin(frontendUrl);

        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");

        // preflight 요청의 캐시 시간을 1시간으로 설정
        configuration.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", configuration);
        return new CorsFilter(source);
    }
}
