package jyoungmin.vocabauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Data transfer object for JWT token pairs.
 * Contains both access and refresh tokens for authentication.
 */
@Builder
@AllArgsConstructor
@Data
public class JwtToken {
    /**
     * Token type (typically "Bearer")
     */
    private String grantType;

    /**
     * Short-lived access token for API requests
     */
    private String accessToken;

    /**
     * Long-lived refresh token for obtaining new access tokens
     */
    private String refreshToken;
}
