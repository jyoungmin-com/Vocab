package jyoungmin.vocablist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WordRequest {
    @NotBlank(message = "Word is required")
    private String word;

    @NotBlank(message = "Meaning is required")
    private String meaning;

    private boolean isMemorized; // Optional, Default: false
    private String furigana;  // Optional, but validated by JapaneseValidator if word contains Japanese
    private Long listId;  // Optional, use Default list if null
}
