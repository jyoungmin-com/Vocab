package jyoungmin.vocabauth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jyoungmin.vocabauth.dao.RedisDao;
import jyoungmin.vocabauth.dto.JwtToken;
import jyoungmin.vocabauth.exception.AuthException;
import jyoungmin.vocabauth.exception.ErrorCode;
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

// JWT create, validate
@Slf4j
@Component
public class JwtTokenProvider {
    private final Key key;
    private final UserDetailsService userDetailsService;
    private final RedisDao redisDao;

    private static final String GRANT_TYPE = "Bearer";

    @Value("${jwt.accessToken.ExpirationTime}")
    private long ACCESS_TOKEN_EXPIRE_TIME;

    @Value("${jwt.refreshToken.ExpirationTime}")
    private long REFRESH_TOKEN_EXPIRE_TIME;





    // Secretkey config
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            UserDetailsService userDetailsService,
                            RedisDao redisDao) {
        byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes());
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.userDetailsService = userDetailsService;
        this.redisDao = redisDao;
    }







    // Generate access/refresh token w/ user info
    public JwtToken generateToken(Authentication authentication) {
        // get autho
        // JWT 토큰의 claims에 포함되어 사용자의 권한 정보를 저장하는데 사용됨
        String authorities = authentication.getAuthorities().stream() // Authentication 객체에서 사용자 권한 목록 가져오기
                .map(GrantedAuthority::getAuthority) // 각 GrantedAuthority 객체에서 권한 문자열만 추출하기
                .collect(Collectors.joining(",")); // 추출된 권한 문자열들을 쉼표로 구분하여 하나의 문자열로 결합하기

        long now = (new Date()).getTime();
        String username = authentication.getName();

        // create access token
        Date accessTokenExpire = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = generateAccessToken(username, authorities, accessTokenExpire);

        // create refresh token
        Date refreshTokenExpire = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);
        String refreshToken = generateRefreshToken(username, refreshTokenExpire);

        // input refresh token into Redis
        // delete after designated time
        redisDao.setValues(username, refreshToken, Duration.ofMillis(REFRESH_TOKEN_EXPIRE_TIME));

        return JwtToken.builder().
                grantType(GRANT_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public JwtToken generateTokenWithRefreshToken(String username) {
        long now = (new Date()).getTime();

        // create access token
        Date accessTokenExpire = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        //get user info w/ UserDetailsService
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        String accessToken = generateAccessToken(username, authorities, accessTokenExpire);

        // create refresh token
        Date refreshTokenExpire = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);
        String refreshToken = generateRefreshToken(username, refreshTokenExpire);

        // save new refresh token into redis
        redisDao.setValues(username, refreshToken, Duration.ofMillis(REFRESH_TOKEN_EXPIRE_TIME));

        return JwtToken.builder()
                .grantType(GRANT_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private String generateAccessToken(String username, String authorities, Date expireDate) {
        return Jwts.builder()
                .subject(username) // 토큰 제목 (사용자 이름)
                .claim("auth", authorities) // 권한 정보 (커스텀 클레임)
                .expiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact(); // 최종 JWT 문자열 생성 (header.payload.signature 구조);
    }

    private String generateRefreshToken(String username, Date expireDate) {
        return Jwts.builder()
                .subject(username)
                .expiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }








    //JWT 토큰을 복호화하여 토큰에 들어있는 인증 정보를 꺼내는 메서드
    public Authentication getAuthentication(String accessToken) {
        // JWT decryp
        Claims claims = parseClaims(accessToken);
        Object authClaim = claims.get("auth");
        if (authClaim == null) {
            throw new AuthException(
                    ErrorCode.INVALID_TOKEN,
                    "Authority information missing from token"
            );
        }

        // get auth info from claim
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(authClaim.toString().split(","))
                .map(SimpleGrantedAuthority::new) // SimpleGrantedAuthority 객체들의 컬렉션으로 변환
                .toList();

        // UserDetails 객체를 만들어서 Authentication return
        // UserDetails: interface, User: UserDetails를 구현한 클래스
        UserDetails principal = new User(claims.getSubject(), "", authorities); // 파라미터: 사용자 식별자, credentials, 권한 목록
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    // JWT decryp
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(accessToken) // JWT 토큰 검증과 파싱을 모두 수행함
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }







    // Validate Token info
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            throw e; // Re-throw to be caught by filter
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            return false;
        }
    }

    // validate refresh token
    public boolean validateRefreshToken(String token) {
        // basic validation
        if (!validateToken(token)) return false;

        try {
            // get username from token
            String username = getUserNameFromToken(token);

            // compare w/ refresh token from Redis
            String redisToken = (String) redisDao.getValues(username);
            return token.equals(redisToken);
        } catch (Exception e) {
            log.info("Refresh token validation failed", e);
            return false;
        }
    }

    // get username from token
    public String getUserNameFromToken(String token) {
        try {
            // get claim by parsing token
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // return user name
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        }
    }




    // delete refresh token from redis after logout
    public void deleteRefreshToken(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        redisDao.deleteValues(username);
    }
}
