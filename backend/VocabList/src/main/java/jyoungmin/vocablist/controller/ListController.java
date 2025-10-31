package jyoungmin.vocablist.controller;

import jakarta.validation.Valid;
import jyoungmin.vocabcommons.dto.UserInfo;
import jyoungmin.vocablist.dto.ListRequest;
import jyoungmin.vocablist.service.ListService;
import jyoungmin.vocablist.util.AuthUser;
import jyoungmin.vocabcommons.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for vocabulary list operations.
 * Handles retrieval and creation of user's vocabulary lists.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/list")
public class ListController {
    /**
     * Utility for accessing authenticated user information
     */
    private final AuthUser authUser;

    /**
     * Service for list operations
     */
    private final ListService listService;

    /**
     * Retrieves all lists belonging to the authenticated user.
     *
     * @return response containing user's vocabulary lists
     */
    @GetMapping
    public ResponseEntity<ApiResponse<java.util.List<jyoungmin.vocablist.entity.List>>> getListByUser() {
        UserInfo userInfo = authUser.getUserInfo();

        java.util.List<jyoungmin.vocablist.entity.List> lists = listService.getAllListsByUserId(userInfo.getId());
        return ResponseEntity.ok(ApiResponse.success(ApiResponse.Messages.LISTS_RETRIEVED, lists));
    }

    /**
     * Creates a new vocabulary list for the authenticated user.
     *
     * @param listRequest the request containing list name
     * @return response containing the created list
     */
    @PostMapping
    public ResponseEntity<ApiResponse<jyoungmin.vocablist.entity.List>> createList(@Valid @RequestBody ListRequest listRequest) {
        UserInfo userInfo = authUser.getUserInfo();

        jyoungmin.vocablist.entity.List newList = listService.createList(userInfo.getId(), listRequest.getListName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED, ApiResponse.Messages.LIST_CREATED, newList));
    }
}
