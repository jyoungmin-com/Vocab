package jyoungmin.vocablist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for list creation requests.
 * Contains the name for a new vocabulary list.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListRequest {
    /**
     * Name for the new vocabulary list
     */
    @NotBlank(message = "List name is required")
    private String listName;
}
