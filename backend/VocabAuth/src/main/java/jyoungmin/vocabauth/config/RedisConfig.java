package jyoungmin.vocabauth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for managing refresh tokens and session data.
 * Configures Redis connection using Lettuce client and sets up serialization strategies.
 */
@Configuration
@EnableRedisRepositories
public class RedisConfig {

    /**
     * Redis server hostname
     */
    @Value("${spring.data.redis.host}")
    private String host;

    /**
     * Redis server port
     */
    @Value("${spring.data.redis.port}")
    private int port;

    /**
     * Creates a Redis connection factory using Lettuce client.
     * Configures standalone Redis connection with host and port from application properties.
     *
     * @return configured Redis connection factory
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);

        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    /**
     * Configures RedisTemplate for performing Redis operations.
     * Sets up string serialization for keys and values to ensure data consistency.
     *
     * @return configured RedisTemplate for String keys and Object values
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        // Configure serialization for standard key-value operations
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        // Configure serialization for hash operations
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}
