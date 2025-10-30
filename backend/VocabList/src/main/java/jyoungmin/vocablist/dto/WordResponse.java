package jyoungmin.vocablist.dto;

import jyoungmin.vocablist.entity.Word;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WordResponse {
    private boolean isDuplicated;
    private boolean isJapanese;
    private Word word;
}