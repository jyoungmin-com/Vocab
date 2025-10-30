package jyoungmin.vocablist.util;

import jyoungmin.vocablist.exception.ErrorCode;
import jyoungmin.vocablist.exception.VocabException;
import org.springframework.stereotype.Component;

@Component
public class JapaneseValidator {

    /**
     * 문자열에 일본어 문자(히라가나, 가타카나, 한자)가 포함되어 있는지 확인
     *
     * @param text 확인할 문자열
     * @return 일본어 문자가 포함되어 있으면 true
     */
    public boolean containsJapanese(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        for (char c : text.toCharArray()) {
            // ひらがな: U+3040 ~ U+309F
            // カタカナ: U+30A0 ~ U+30FF
            // 漢字: U+4E00 ~ U+9FAF
            if ((c >= '\u3040' && c <= '\u309F') ||  // 히라가나
                (c >= '\u30A0' && c <= '\u30FF') ||  // 가타카나
                (c >= '\u4E00' && c <= '\u9FAF')) {  // 한자
                return true;
            }
        }

        return false;
    }

    /**
     * 일본어 단어에 후리가나가 필수인지 검증.
     *
     * @param word     단어
     * @param furigana 후리가나
     * @throws VocabException 일본어 단어인데 furigana가 없으면 예외 발생
     */
    public void validateFurigana(String word, String furigana) {
        if (containsJapanese(word)) {
            if (furigana == null || furigana.trim().isEmpty()) {
                throw new VocabException(
                        ErrorCode.FURIGANA_REQUIRED,
                        "Word '" + word + "' contains Japanese characters and requires furigana"
                );
            }
        }
    }
}
