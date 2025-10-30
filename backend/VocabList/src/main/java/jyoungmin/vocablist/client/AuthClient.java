package jyoungmin.vocablist.client;

import jyoungmin.vocablist.dto.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "vocabauth", url = "${server.auth.url}")
public interface AuthClient {

    @GetMapping("/auth/me")
    UserInfo getAuthenticatedUser(@RequestHeader("Authorization") String authorizationHeader);
}
