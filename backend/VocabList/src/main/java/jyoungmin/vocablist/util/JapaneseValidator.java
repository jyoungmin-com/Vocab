package jyoungmin.vocablist.util;

import jyoungmin.vocabcommons.exception.ErrorCode;
import jyoungmin.vocablist.exception.VocabException;
import org.springframework.stereotype.Component;

/**
 * Validator for Japanese text and furigana formatting.
 * Checks for presence of Japanese characters and validates furigana requirements.
 */
@Component
public class JapaneseValidator {

    /**
     * Checks if a string contains Japanese characters (hiragana, katakana, or kanji).
     *
     * @param text the string to check
     * @return true if the text contains Japanese characters
     */
    public boolean containsJapanese(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        for (char c : text.toCharArray()) {
            // Hiragana: U+3040 ~ U+309F
            // Katakana: U+30A0 ~ U+30FF
            // Kanji: U+4E00 ~ U+9FAF
            if ((c >= '\u3040' && c <= '\u309F') ||  // Hiragana
                    (c >= '\u30A0' && c <= '\u30FF') ||  // Katakana
                    (c >= '\u4E00' && c <= '\u9FAF')) {  // Kanji
                return true;
            }
        }

        return false;
    }

    /**
     * Validates that furigana consists only of hiragana or katakana characters.
     *
     * @param furigana the furigana string to validate
     * @return true if the furigana format is valid
     */
    public boolean isValidFurigana(String furigana) {
        if (furigana == null || furigana.isEmpty()) {
            return false;
        }

        for (char c : furigana.toCharArray()) {
            // Allow only whitespace, hiragana, and katakana
            // Hiragana: U+3040 ~ U+309F
            // Katakana: U+30A0 ~ U+30FF (includes middle dot ・ at U+30FB)
            if (!Character.isWhitespace(c) &&
                    !(c >= '\u3040' && c <= '\u309F') &&  // Hiragana
                    !(c >= '\u30A0' && c <= '\u30FF')) {  // Katakana (includes middle dot)
                return false;
            }
        }

        return true;
    }

    /**
     * Validates that Japanese words have required furigana and checks format.
     * Throws an exception if a Japanese word lacks furigana or has invalid format.
     *
     * @param word     the word to validate
     * @param furigana the furigana reading
     * @throws VocabException if Japanese word lacks furigana or format is invalid
     */
    public void validateFurigana(String word, String furigana) {
        if (containsJapanese(word)) {
            if (furigana == null || furigana.trim().isEmpty()) {
                throw new VocabException(
                        ErrorCode.FURIGANA_REQUIRED,
                        "Word '" + word + "' contains Japanese characters and requires furigana"
                );
            }

            // 후리가나 형식 검증 추가
            if (!isValidFurigana(furigana)) {
                throw new VocabException(
                        ErrorCode.INVALID_INPUT,
                        "Furigana '" + furigana + "' must contain only hiragana or katakana characters"
                );
            }
        }
    }
}
