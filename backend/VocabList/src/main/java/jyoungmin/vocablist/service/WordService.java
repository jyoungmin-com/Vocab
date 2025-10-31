package jyoungmin.vocablist.service;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jyoungmin.vocabcommons.dto.UserInfo;
import jyoungmin.vocablist.dto.WordRequest;
import jyoungmin.vocablist.dto.WordResponse;
import jyoungmin.vocablist.entity.Word;
import jyoungmin.vocabcommons.exception.ErrorCode;
import jyoungmin.vocablist.exception.VocabException;
import jyoungmin.vocablist.repository.WordRepository;
import jyoungmin.vocablist.util.AuthUser;
import jyoungmin.vocablist.util.JapaneseValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing vocabulary words.
 * Handles word CRUD operations, Japanese text validation, and list assignment.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WordService {
    /**
     * Repository for word data access
     */
    private final WordRepository wordRepository;

    /**
     * Utility for accessing authenticated user information
     */
    private final AuthUser authUser;

    /**
     * Service for list operations
     */
    private final ListService listService;

    /**
     * Validator for Japanese text and furigana
     */
    private final JapaneseValidator japaneseValidator;

    /**
     * Saves a new word to the database.
     * Checks for duplicates and returns appropriate response.
     *
     * @param wordRequest the word data to save
     * @return response indicating whether word was saved or is a duplicate
     */
    @RateLimiter(name = "word-create")
    public WordResponse saveWordToDb(WordRequest wordRequest) {
        Word word = requestToWord(wordRequest);

        if (findWord(word) == null) {
            Word savedWord = wordRepository.save(word);
            log.info("Word created: id={}, word='{}', listId={}, userId={}",
                    savedWord.getId(), savedWord.getWord(), savedWord.getListId(), savedWord.getUserId());
            return toResponse(false, savedWord);
        } else {
            log.info("Duplicate word detected: word='{}', userId={}", word.getWord(), word.getUserId());
            return toResponse(true, word);
        }
    }

    /**
     * Retrieves all words in a specific list.
     * Verifies list ownership before returning words.
     *
     * @param listId the list ID to retrieve words from
     * @return list of words in the specified list
     * @throws VocabException if list not found or access denied
     */
    @RateLimiter(name = "word-general")
    public List<WordResponse> getWordsByListId(long listId) {
        long userId = authUser.getUserInfo().getId();

        // Check if list exists first
        if (!authUser.listExists(listId)) {
            throw new VocabException(
                    ErrorCode.LIST_NOT_FOUND,
                    "List " + listId + " does not exist"
            );
        }

        // Then check ownership
        if (!authUser.verifyListOwner(userId, listId)) {
            throw new VocabException(
                    ErrorCode.LIST_ACCESS_DENIED,
                    "User " + userId + " does not have access to list " + listId
            );
        }

        List<Word> wordList = wordRepository.getWordsByListId(listId);
        return wordList.stream().map(s -> toResponse(false, s)).toList();
    }

    /**
     * Retrieves all words belonging to the current user.
     *
     * @return list of all user's words across all lists
     */
    @RateLimiter(name = "word-general")
    public List<WordResponse> getWordsByUserId() {
        return wordRepository.getWordsByUserId(authUser.getUserInfo().getId())
                .stream()
                .map(s -> toResponse(false, s))
                .toList();
    }

    /**
     * Retrieves a specific word by ID.
     * Verifies ownership before returning the word.
     *
     * @param wordId the word ID to retrieve
     * @return the word data
     * @throws VocabException if word not found for user
     */
    public WordResponse getWordById(long wordId) {
        long userId = authUser.getUserInfo().getId();
        List<Word> words = wordRepository.findByIdAndUserId(wordId, userId);

        if (words.isEmpty()) {
            throw new VocabException(
                    ErrorCode.WORD_NOT_FOUND,
                    "Word with id " + wordId + " not found for user " + userId
            );
        }

        return toResponse(false, words.get(0));
    }

    /**
     * Deletes a word by ID.
     * Verifies ownership before deletion.
     *
     * @param wordId the word ID to delete
     * @return true if deletion was successful
     * @throws VocabException if word not found for user
     */
    @RateLimiter(name = "word-general")
    public boolean deleteWordById(long wordId) {
        long userId = authUser.getUserInfo().getId();
        Word word = wordRepository.getWordByidAndUserId(wordId, userId);
        if (word != null) {
            log.info("Deleting word: id={}, word='{}', userId={}", wordId, word.getWord(), userId);
            wordRepository.deleteById(wordId);
            return true;
        } else {
            throw new VocabException(
                    ErrorCode.WORD_NOT_FOUND,
                    "Word with id " + wordId + " not found for user " + userId
            );
        }
    }

    /**
     * Updates an existing word.
     * Validates furigana for Japanese words and verifies list access.
     *
     * @param wordId      the word ID to update
     * @param wordRequest the new word data
     * @return the updated word
     * @throws VocabException if word not found, list not found, or access denied
     */
    @RateLimiter(name = "word-general")
    public WordResponse updateWordById(long wordId, WordRequest wordRequest) {
        long userId = authUser.getUserInfo().getId();
        Word word = wordRepository.getWordByidAndUserId(wordId, userId);

        if (word == null) {
            throw new VocabException(
                    ErrorCode.WORD_NOT_FOUND,
                    "Word with id " + wordId + " not found for user " + userId
            );
        }

        // Validate furigana for Japanese words
        japaneseValidator.validateFurigana(wordRequest.getWord(), wordRequest.getFurigana());

        // Determine listId
        long listId;
        if (wordRequest.getListId() != null) {
            listId = wordRequest.getListId();

            // Check if list exists first
            if (!authUser.listExists(listId)) {
                throw new VocabException(
                        ErrorCode.LIST_NOT_FOUND,
                        "List " + listId + " does not exist"
                );
            }

            // Then check ownership
            if (!authUser.verifyListOwner(userId, listId)) {
                throw new VocabException(
                        ErrorCode.LIST_ACCESS_DENIED,
                        "User " + userId + " does not have access to list " + listId
                );
            }
        } else {
            // Keep the existing listId if not provided
            listId = word.getListId();
        }

        // Update the existing word entity
        word.setWord(wordRequest.getWord());
        word.setMeaning(wordRequest.getMeaning());
        word.setFurigana(wordRequest.getFurigana());
        word.setMemorized(wordRequest.isMemorized());
        word.setListId(listId);

        Word updatedWord = wordRepository.save(word);
        log.info("Word updated: id={}, word='{}', listId={}, userId={}",
                updatedWord.getId(), updatedWord.getWord(), updatedWord.getListId(), userId);
        return toResponse(false, updatedWord);
    }


    /**
     * Finds an existing word for the user.
     *
     * @param word the word to search for
     * @return the existing word or null if not found
     */
    private Word findWord(Word word) {
        List<Word> existingList = wordRepository.findByWordAndUserId(word.getWord(), word.getUserId());
        if (existingList.isEmpty()) {
            return null;
        } else {
            return existingList.get(0);
        }
    }

    /**
     * Converts a word request to a Word entity.
     * Validates Japanese words, assigns list, and populates user information.
     *
     * @param wordRequest the request containing word data
     * @return Word entity ready to be saved
     * @throws VocabException if validation fails or list access denied
     */
    private Word requestToWord(WordRequest wordRequest) {
        UserInfo userInfo = authUser.getUserInfo();
        long userId = userInfo.getId();

        // Validate furigana requirement for Japanese words
        japaneseValidator.validateFurigana(wordRequest.getWord(), wordRequest.getFurigana());

        // Use default list if listId not specified
        long listId;
        if (wordRequest.getListId() != null) {
            listId = wordRequest.getListId();

            // Check if list exists first
            if (!authUser.listExists(listId)) {
                throw new VocabException(
                        ErrorCode.LIST_NOT_FOUND,
                        "List " + listId + " does not exist"
                );
            }

            // Then check ownership
            if (!authUser.verifyListOwner(userId, listId)) {
                throw new VocabException(
                        ErrorCode.LIST_ACCESS_DENIED,
                        "User " + userId + " does not have access to list " + listId
                );
            }
        } else {
            // Get or create user's default list (auto-created on first login)
            jyoungmin.vocablist.entity.List defaultList = listService.getOrCreateDefaultList(userId);
            listId = defaultList.getId();
        }

        Word word = Word.builder()
                .isMemorized(wordRequest.isMemorized())
                .word(wordRequest.getWord())
                .meaning(wordRequest.getMeaning())
                .furigana(wordRequest.getFurigana())
                .userId(userId)
                .listId(listId)
                .build();

        return word;
    }

    /**
     * Converts a Word entity to a response DTO.
     * Includes duplication status and Japanese detection.
     *
     * @param isDuplicated whether the word is a duplicate
     * @param word         the word entity
     * @return word response DTO
     */
    private WordResponse toResponse(boolean isDuplicated, Word word) {
        return WordResponse.builder()
                .isDuplicated(isDuplicated)
                .isJapanese(japaneseValidator.containsJapanese(word.getWord()))
                .word(word)
                .build();
    }
}
