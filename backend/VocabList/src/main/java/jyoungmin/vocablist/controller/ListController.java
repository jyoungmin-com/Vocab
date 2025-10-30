package jyoungmin.vocablist.controller;

import jakarta.validation.Valid;
import jyoungmin.vocablist.dto.ListRequest;
import jyoungmin.vocablist.dto.UserInfo;
import jyoungmin.vocablist.service.ListService;
import jyoungmin.vocablist.util.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/list")
public class ListController {
    private final AuthUser authUser;
    private final ListService listService;


    @GetMapping
    public ResponseEntity getListByUser() {
        UserInfo userInfo = authUser.getUserInfo();

        return ResponseEntity.ok(listService.getAllListsByUserId(userInfo.getId()));
    }

    @PostMapping
    public ResponseEntity createList(@Valid @RequestBody ListRequest listRequest) {
        UserInfo userInfo = authUser.getUserInfo();

        return ResponseEntity.ok(listService.createList(userInfo.getId(), listRequest.getListName()));
    }
}
