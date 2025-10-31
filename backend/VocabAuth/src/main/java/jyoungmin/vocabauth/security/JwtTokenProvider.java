package jyoungmin.vocabauth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import io.jsonwebtoken.security.SignatureException;
import jyoungmin.vocabauth.dao.RedisDao;
import jyoungmin.vocabauth.dto.JwtToken;
import jyoungmin.vocabauth.exception.AuthException;
import jyoungmin.vocabcommons.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Provider for creating and validating JWT tokens.
 * Handles both access and refresh token generation, validation, and management with Redis storage.
 */
@Slf4j
@Component
public class JwtTokenProvider {
    /**
     * Secret key used for signing JWT tokens
     */
    private final Key key;

    /**
     * Service for loading user authentication details
     */
    private final UserDetailsService userDetailsService;

    /**
     * DAO for managing refresh tokens in Redis
     */
    private final RedisDao redisDao;

    /**
     * Token type prefix for authorization header
     */
    private static final String GRANT_TYPE = "Bearer";

    /**
     * Access token expiration time in milliseconds
     */
    @Value("${jwt.accessToken.ExpirationTime}")
    private long ACCESS_TOKEN_EXPIRE_TIME;

    /**
     * Refresh token expiration time in milliseconds
     */
    @Value("${jwt.refreshToken.ExpirationTime}")
    private long REFRESH_TOKEN_EXPIRE_TIME;

    /**
     * Constructs a JWT token provider with the given secret key.
     * Initializes the signing key using HMAC-SHA algorithm.
     *
     * @param secretKey          the secret key for signing tokens
     * @param userDetailsService service for loading user details
     * @param redisDao           DAO for Redis operations
     */
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            UserDetailsService userDetailsService,
                            RedisDao redisDao) {
        byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes());
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.userDetailsService = userDetailsService;
        this.redisDao = redisDao;
    }


    /**
     * Generates both access and refresh tokens for the authenticated user.
     * Stores the refresh token in Redis with an expiration time.
     *
     * @param authentication the authentication object containing user details
     * @return JWT token pair (access and refresh tokens)
     */
    public JwtToken generateToken(Authentication authentication) {
        // Extract and format user authorities as comma-separated string for JWT claims
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        String username = authentication.getName();

        // Generate access token with user info and authorities
        Date accessTokenExpire = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = generateAccessToken(username, authorities, accessTokenExpire);

        // Generate refresh token
        Date refreshTokenExpire = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);
        String refreshToken = generateRefreshToken(username, refreshTokenExpire);

        // Store refresh token in Redis with expiration
        redisDao.setValues(username, refreshToken, Duration.ofMillis(REFRESH_TOKEN_EXPIRE_TIME));

        return JwtToken.builder().
                grantType(GRANT_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Generates new token pair using a refresh token.
     * Loads fresh user details and creates new access and refresh tokens.
     *
     * @param username the username to generate tokens for
     * @return new JWT token pair
     */
    public JwtToken generateTokenWithRefreshToken(String username) {
        long now = (new Date()).getTime();

        // Create new access token with fresh user details
        Date accessTokenExpire = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        String accessToken = generateAccessToken(username, authorities, accessTokenExpire);

        // Create new refresh token
        Date refreshTokenExpire = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);
        String refreshToken = generateRefreshToken(username, refreshTokenExpire);

        // Update refresh token in Redis
        redisDao.setValues(username, refreshToken, Duration.ofMillis(REFRESH_TOKEN_EXPIRE_TIME));

        return JwtToken.builder()
                .grantType(GRANT_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Generates an access token with user information and authorities.
     *
     * @param username    the username (token subject)
     * @param authorities comma-separated list of user authorities
     * @param expireDate  token expiration date
     * @return signed JWT access token
     */
    private String generateAccessToken(String username, String authorities, Date expireDate) {
        return Jwts.builder()
                .subject(username)
                .claim("auth", authorities)
                .expiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generates a refresh token with minimal claims.
     *
     * @param username   the username (token subject)
     * @param expireDate token expiration date
     * @return signed JWT refresh token
     */
    private String generateRefreshToken(String username, Date expireDate) {
        return Jwts.builder()
                .subject(username)
                .expiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    /**
     * Extracts authentication information from a JWT access token.
     * Parses the token and creates an Authentication object with user details and authorities.
     *
     * @param accessToken the JWT access token to parse
     * @return authentication object containing user details
     * @throws AuthException if authority information is missing from token
     */
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        Object authClaim = claims.get("auth");
        if (authClaim == null) {
            throw new AuthException(
                    ErrorCode.INVALID_TOKEN,
                    "Authority information missing from token"
            );
        }

        // Extract authorities from claims
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(authClaim.toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .toList();

        // Create UserDetails and return Authentication object
        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * Parses JWT claims from an access token.
     * Returns claims even if the token is expired for graceful handling.
     *
     * @param accessToken the JWT token to parse
     * @return parsed claims from the token
     */
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }


    /**
     * Validates a JWT token by parsing and verifying its signature.
     * Throws appropriate exceptions for different validation failures.
     *
     * @param token the JWT token to validate
     * @return true if token is valid
     * @throws AuthException if token is invalid, expired, or malformed
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            throw new AuthException(ErrorCode.TOKEN_EXPIRED, e.getMessage());
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            throw new AuthException(ErrorCode.INVALID_TOKEN, e.getMessage());
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT format or signature: {}", e.getMessage());
            throw new AuthException(ErrorCode.INVALID_TOKEN, e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            throw new AuthException(ErrorCode.INVALID_TOKEN, e.getMessage());
        } catch (InvalidClaimException e) {
            log.warn("Invalid JWT claims: {}", e.getMessage());
            throw new AuthException(ErrorCode.INVALID_TOKEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            throw new AuthException(ErrorCode.INVALID_TOKEN, e.getMessage());
        }
    }

    /**
     * Validates a refresh token by checking its format and comparing with stored value in Redis.
     * Ensures the token matches the one stored for the user.
     *
     * @param token the refresh token to validate
     * @return true if refresh token is valid and matches stored value
     * @throws AuthException if token is invalid, not found, or doesn't match stored value
     */
    public boolean validateRefreshToken(String token) {
        if (!validateToken(token)) return false;

        try {
            String username = getUserNameFromToken(token);

            // Verify token matches the one stored in Redis
            return redisDao.getValues(username)
                    .map(redisToken -> {
                        if (!token.equals(redisToken.toString())) {
                            throw new AuthException(
                                    ErrorCode.REFRESH_TOKEN_INVALID,
                                    "Refresh token does not match stored token"
                            );
                        }
                        return true;
                    })
                    .orElseThrow(() -> new AuthException(
                            ErrorCode.REFRESH_TOKEN_NOT_FOUND,
                            "Refresh token not found in store (user may have logged out)"
                    ));
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Refresh token validation failed: {}", e.getMessage());
            throw new AuthException(
                    ErrorCode.REFRESH_TOKEN_INVALID,
                    "Failed to validate refresh token: " + e.getMessage()
            );
        }
    }

    /**
     * Extracts the username from a JWT token.
     * Returns username even if the token is expired.
     *
     * @param token the JWT token to parse
     * @return username extracted from token subject
     */
    public String getUserNameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        }
    }

    /**
     * Deletes a refresh token from Redis storage.
     * Called during logout to invalidate the refresh token.
     *
     * @param username the username whose refresh token should be deleted
     * @throws IllegalArgumentException if username is null or empty
     */
    public void deleteRefreshToken(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        redisDao.deleteValues(username);
    }
}
