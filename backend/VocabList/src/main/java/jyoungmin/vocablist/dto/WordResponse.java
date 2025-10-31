package jyoungmin.vocablist.dto;

import jyoungmin.vocablist.entity.Word;
import lombok.Builder;
import lombok.Data;

/**
 * Data transfer object for word responses.
 * Includes metadata about duplication and Japanese text detection.
 */
@Data
@Builder
public class WordResponse {
    /**
     * Whether this word already exists for the user
     */
    private boolean isDuplicated;

    /**
     * Whether the word contains Japanese characters
     */
    private boolean isJapanese;

    /**
     * The word entity with all details
     */
    private Word word;
}