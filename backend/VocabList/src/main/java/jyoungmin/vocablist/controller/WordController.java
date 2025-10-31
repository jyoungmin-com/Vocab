package jyoungmin.vocablist.controller;

import jakarta.validation.Valid;
import jyoungmin.vocabcommons.response.ApiResponse;
import jyoungmin.vocablist.dto.WordRequest;
import jyoungmin.vocablist.dto.WordResponse;
import jyoungmin.vocablist.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for vocabulary word operations.
 * Handles CRUD operations for words including Japanese text validation.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/word")
public class WordController {

    /**
     * Service for word operations
     */
    private final WordService wordService;

    /**
     * Saves a new word to the database.
     * If listId is specified, saves to that list; otherwise uses default list.
     * Automatically creates a "Default" list if user has no lists.
     *
     * @param wordRequest the word data (word, meaning, listId-optional)
     * @return response containing save result
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<WordResponse>> saveWord(@Valid @RequestBody WordRequest wordRequest) {
        WordResponse savedWord = wordService.saveWordToDb(wordRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED, ApiResponse.Messages.WORD_CREATED, savedWord));
    }

    /**
     * Retrieves all words in a specific list.
     *
     * @param listId the list ID to retrieve words from
     * @return response containing words in the list
     */
    @GetMapping(params = "listId")
    public ResponseEntity<ApiResponse<java.util.List<WordResponse>>> getWordsByListId(@RequestParam long listId) {
        java.util.List<WordResponse> words = wordService.getWordsByListId(listId);
        return ResponseEntity.ok(ApiResponse.success(ApiResponse.Messages.WORDS_RETRIEVED, words));
    }

    /**
     * Retrieves all words belonging to the authenticated user.
     *
     * @return response containing all user's words
     */
    @GetMapping
    public ResponseEntity<ApiResponse<java.util.List<WordResponse>>> getWordsByUserId() {
        java.util.List<WordResponse> words = wordService.getWordsByUserId();
        return ResponseEntity.ok(ApiResponse.success(ApiResponse.Messages.WORDS_RETRIEVED, words));
    }

    /**
     * Deletes a word by ID.
     *
     * @param wordId the word ID to delete
     * @return response indicating deletion success
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteWordByWordId(@RequestParam long wordId) {
        wordService.deleteWordById(wordId);
        return ResponseEntity.ok(ApiResponse.success(ApiResponse.Messages.WORD_DELETED));
    }

    /**
     * Updates an existing word.
     *
     * @param wordId      the word ID to update
     * @param wordRequest the new word data
     * @return response containing the updated word
     */
    @PatchMapping
    public ResponseEntity<ApiResponse<WordResponse>> updateWordByWordId(@RequestParam long wordId,
                                                                        @Valid @RequestBody WordRequest wordRequest) {
        WordResponse updatedWord = wordService.updateWordById(wordId, wordRequest);
        return ResponseEntity.ok(ApiResponse.success(ApiResponse.Messages.WORD_UPDATED, updatedWord));
    }
}
