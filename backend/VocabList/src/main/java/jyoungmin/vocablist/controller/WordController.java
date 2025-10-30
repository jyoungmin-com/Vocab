package jyoungmin.vocablist.controller;

import jakarta.validation.Valid;
import jyoungmin.vocablist.dto.UserInfo;
import jyoungmin.vocablist.dto.WordRequest;
import jyoungmin.vocablist.entity.List;
import jyoungmin.vocablist.entity.Word;
import jyoungmin.vocablist.service.ListService;
import jyoungmin.vocablist.service.WordService;
import jyoungmin.vocablist.util.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/word")
public class WordController {

    private final WordService wordService;
    private final ListService listService;
    private final AuthUser authUser;

    /**
     * 단어를 저장합니다.
     * listId를 지정하면 해당 리스트에 저장하고, 지정하지 않으면 기본 리스트에 저장합니다.
     * 사용자의 리스트가 없으면 자동으로 "Default" 리스트를 생성합니다.
     *
     * @param wordRequest 저장할 단어 정보 (word, meaning, listId-선택)
     * @return 저장 결과
     */
    @PostMapping("/save")
    public ResponseEntity saveWord(@Valid @RequestBody WordRequest wordRequest) {
        return ResponseEntity.ok(wordService.saveWordToDb(wordRequest));
    }


    @GetMapping(params = "listId")
    public ResponseEntity getWordsByListId(@RequestParam long listId) {
        return ResponseEntity.ok(wordService.getWordsByListId(listId));
    }

    @GetMapping
    public ResponseEntity getWordsByUserId() {
        return ResponseEntity.ok(wordService.getWordsByUserId());
    }

    @DeleteMapping
    public ResponseEntity deleteWordByWordId(@RequestParam long wordId) {
        wordService.deleteWordById(wordId);
        return ResponseEntity.ok("Deleted");
    }

    @PatchMapping
    public ResponseEntity updateWordByWordId(@RequestParam long wordId,
                                             @Valid @RequestBody WordRequest wordRequest) {
        return ResponseEntity.ok(wordService.updateWordById(wordId, wordRequest));
    }
}
