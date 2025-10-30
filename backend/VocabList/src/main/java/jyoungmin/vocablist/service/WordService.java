package jyoungmin.vocablist.service;

import jyoungmin.vocablist.dto.UserInfo;
import jyoungmin.vocablist.dto.WordRequest;
import jyoungmin.vocablist.dto.WordResponse;
import jyoungmin.vocablist.entity.Word;
import jyoungmin.vocablist.exception.ErrorCode;
import jyoungmin.vocablist.exception.VocabException;
import jyoungmin.vocablist.repository.WordRepository;
import jyoungmin.vocablist.util.AuthUser;
import jyoungmin.vocablist.util.JapaneseValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WordService {
    private final WordRepository wordRepository;
    private final AuthUser authUser;
    private final ListService listService;
    private final JapaneseValidator japaneseValidator;

    public WordResponse saveWordToDb(WordRequest wordRequest) {
        Word word = requestToWord(wordRequest);

        if (findWord(word) == null) {
            return toResponse(false, wordRepository.save(word));
        } else {
            return toResponse(true, word);
        }
    }

    public List<WordResponse> getWordsByListId(long listId) {
        long userId = authUser.getUserInfo().getId();
        if (!authUser.verifyListOwner(userId, listId)) {
            throw new VocabException(
                    ErrorCode.LIST_ACCESS_DENIED,
                    "User " + userId + " does not have access to list " + listId
            );
        }

        List<Word> wordList = wordRepository.getWordsByListId(listId);
        return wordList.stream().map(s -> toResponse(false, s)).toList();
    }

    public List<WordResponse> getWordsByUserId() {
        return wordRepository.getWordsByUserId(authUser.getUserInfo().getId())
                .stream()
                .map(s -> toResponse(false, s))
                .toList();
    }

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

    public boolean deleteWordById(long wordId) {
        long userId = authUser.getUserInfo().getId();
        Word word = wordRepository.getWordByidAndUserId(wordId, userId);
        if (word != null) {
            wordRepository.deleteById(wordId);
            return true;
        } else {
            throw new VocabException(
                    ErrorCode.WORD_NOT_FOUND,
                    "Word with id " + wordId + " not found for user " + userId
            );
        }
    }

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

        return toResponse(false, wordRepository.save(word));
    }


    private Word findWord(Word word) {
        List<Word> existingList = wordRepository.findByWordAndUserId(word.getWord(), word.getUserId());
        if (existingList.isEmpty()) {
            return null;
        } else {
            return existingList.get(0);
        }
    }

    private Word requestToWord(WordRequest wordRequest) {
        UserInfo userInfo = authUser.getUserInfo();
        long userId = userInfo.getId();

        // 일본어 단어인 경우 furigana 필수 검증
        japaneseValidator.validateFurigana(wordRequest.getWord(), wordRequest.getFurigana());

        // listId가 지정되지 않았으면 기본 리스트 사용
        long listId;
        if (wordRequest.getListId() != null) {
            listId = wordRequest.getListId();
        } else {
            // 사용자의 기본 리스트 조회 또는 생성 (처음 로그인 시 자동 생성)
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

    private WordResponse toResponse(boolean isDuplicated, Word word) {
        return WordResponse.builder()
                .isDuplicated(isDuplicated)
                .isJapanese(japaneseValidator.containsJapanese(word.getWord()))
                .word(word)
                .build();
    }
}
