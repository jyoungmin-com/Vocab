package jyoungmin.vocablist.config;

import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Response;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import jyoungmin.vocabcommons.exception.ErrorCode;
import jyoungmin.vocablist.exception.VocabException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign client configuration for inter-service communication.
 * Configures request interceptors, timeouts, retry logic, and error decoding.
 */
@Slf4j
@Configuration
public class FeignConfig {

    /**
     * HTTP header name for correlation ID
     */
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    /**
     * MDC key for correlation ID
     */
    private static final String CORRELATION_ID_LOG_KEY = "correlationId";

    /**
     * Configures request interceptor to propagate correlation ID across services.
     *
     * @return request interceptor with correlation ID header
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                String correlationId = MDC.get(CORRELATION_ID_LOG_KEY);
                if (correlationId != null) {
                    template.header(CORRELATION_ID_HEADER, correlationId);
                }
            }
        };
    }

    /**
     * Configures request timeouts for Feign clients.
     * Connection timeout: 5 seconds, Read timeout: 10 seconds
     *
     * @return configured request options
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(5, TimeUnit.SECONDS, 10, TimeUnit.SECONDS, true);
    }

    /**
     * Configures retry logic for failed requests.
     * Retry period: 100ms, max period: 1s, max attempts: 3
     *
     * @return configured retryer
     */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(100, TimeUnit.SECONDS.toMillis(1), 3);
    }

    /**
     * Configures custom error decoder for Feign responses.
     *
     * @return custom error decoder
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    /**
     * Custom error decoder for converting Feign HTTP errors to domain exceptions.
     * Maps common HTTP status codes to appropriate VocabException instances.
     */
    @Slf4j
    static class FeignErrorDecoder implements ErrorDecoder {
        /**
         * Default decoder for unhandled status codes
         */
        private final ErrorDecoder defaultDecoder = new Default();

        /**
         * Decodes HTTP error responses into domain exceptions.
         *
         * @param methodKey the Feign method that was called
         * @param response  the HTTP response
         * @return decoded exception
         */
        @Override
        public Exception decode(String methodKey, Response response) {
            int status = response.status();

            log.warn("[FeignErrorDecoder] Error response from {}: status={}", methodKey, status);

            return switch (status) {
                case 401 -> new VocabException(
                        ErrorCode.INVALID_TOKEN,
                        "Authentication failed with auth service"
                );
                case 403 -> new VocabException(
                        ErrorCode.UNAUTHORIZED_ACCESS,
                        "Access denied by auth service"
                );
                case 404 -> new VocabException(
                        ErrorCode.USER_NOT_FOUND,
                        "Resource not found in auth service"
                );
                case 503 -> new VocabException(
                        ErrorCode.AUTH_SERVICE_UNAVAILABLE,
                        "Auth service is unavailable"
                );
                default -> {
                    if (status >= 500) {
                        yield new VocabException(
                                ErrorCode.AUTH_SERVICE_ERROR,
                                "Auth service error: " + status
                        );
                    }
                    yield defaultDecoder.decode(methodKey, response);
                }
            };
        }
    }
}
