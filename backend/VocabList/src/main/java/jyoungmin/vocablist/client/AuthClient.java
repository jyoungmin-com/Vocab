package jyoungmin.vocablist.client;

import jyoungmin.vocabcommons.dto.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client for communicating with the VocabAuth authentication service.
 * Provides methods for token validation and user information retrieval.
 */
@FeignClient(name = "vocabauth", url = "${server.auth.url}")
public interface AuthClient {

    /**
     * Retrieves authenticated user information from the auth service.
     * Validates the JWT token and returns user details.
     *
     * @param authorizationHeader the Authorization header containing JWT token
     * @return user information if token is valid
     */
    @GetMapping("/api/v1/auth/me")
    UserInfo getAuthenticatedUser(@RequestHeader("Authorization") String authorizationHeader);
}
