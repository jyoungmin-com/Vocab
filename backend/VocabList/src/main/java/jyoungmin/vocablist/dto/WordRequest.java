package jyoungmin.vocablist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Data transfer object for word creation and update requests.
 * Contains vocabulary word data with optional furigana and list assignment.
 */
@Data
public class WordRequest {
    /**
     * The vocabulary word to save
     */
    @NotBlank(message = "Word is required")
    private String word;

    /**
     * Translation or meaning of the word
     */
    @NotBlank(message = "Meaning is required")
    private String meaning;

    /**
     * Whether the word has been memorized (optional, default: false)
     */
    private boolean isMemorized;

    /**
     * Furigana reading for Japanese words (optional, validated if word contains Japanese)
     */
    private String furigana;

    /**
     * ID of the list to save word to (optional, uses default list if null)
     */
    private Long listId;
}
