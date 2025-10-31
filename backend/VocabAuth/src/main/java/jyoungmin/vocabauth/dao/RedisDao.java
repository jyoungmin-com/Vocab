package jyoungmin.vocabauth.dao;

import jyoungmin.vocabauth.exception.AuthException;
import jyoungmin.vocabcommons.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Data access object for Redis operations.
 * Provides methods for storing, retrieving, and deleting refresh tokens and session data.
 */
@Slf4j
@Component
public class RedisDao {
    /**
     * Template for Redis operations
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Operations for handling String-type values
     */
    private final ValueOperations<String, Object> valueOperations;

    /**
     * Constructs RedisDao with the given Redis template.
     * Initializes value operations for simplified String handling.
     *
     * @param redisTemplate the Redis template for data operations
     */
    public RedisDao(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
    }

    /**
     * Stores data in Redis without expiration time.
     *
     * @param key  the key under which to store the data
     * @param data the data to store
     * @throws AuthException if Redis operation fails
     */
    public void setValues(String key, String data) {
        try {
            valueOperations.set(key, data);
        } catch (DataAccessException e) {
            log.error("[RedisDao] Failed to set value for key: {}", key, e);
            throw new AuthException(
                    ErrorCode.REDIS_ERROR,
                    "Failed to save data to Redis: " + e.getMessage()
            );
        }
    }

    /**
     * Stores data in Redis with an expiration time.
     * Primarily used for storing refresh tokens with TTL.
     *
     * @param key      the key under which to store the data
     * @param data     the data to store
     * @param duration the time-to-live for the data
     * @throws AuthException if Redis operation fails
     */
    public void setValues(String key, String data, Duration duration) {
        try {
            valueOperations.set(key, data, duration);
        } catch (DataAccessException e) {
            log.error("[RedisDao] Failed to set value with duration for key: {}", key, e);
            throw new AuthException(
                    ErrorCode.REDIS_ERROR,
                    "Failed to save data to Redis: " + e.getMessage()
            );
        }
    }

    /**
     * Retrieves data from Redis by key.
     * Used for refresh token validation during authentication.
     *
     * @param key the key to look up
     * @return Optional containing the data if found, empty otherwise
     * @throws AuthException if Redis operation fails
     */
    public Optional<Object> getValues(String key) {
        try {
            return Optional.ofNullable(valueOperations.get(key));
        } catch (DataAccessException e) {
            log.error("[RedisDao] Failed to get value for key: {}", key, e);
            throw new AuthException(
                    ErrorCode.REDIS_ERROR,
                    "Failed to retrieve data from Redis: " + e.getMessage()
            );
        }
    }

    /**
     * Deletes data from Redis by key.
     * Used to remove refresh tokens during logout.
     *
     * @param key the key of the data to delete
     * @throws AuthException if Redis operation fails
     */
    public void deleteValues(String key) {
        try {
            redisTemplate.delete(key);
        } catch (DataAccessException e) {
            log.error("[RedisDao] Failed to delete value for key: {}", key, e);
            throw new AuthException(
                    ErrorCode.REDIS_ERROR,
                    "Failed to delete data from Redis: " + e.getMessage()
            );
        }
    }
}
