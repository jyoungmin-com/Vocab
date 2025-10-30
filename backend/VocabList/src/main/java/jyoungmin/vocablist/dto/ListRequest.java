package jyoungmin.vocablist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListRequest {
    @NotBlank(message = "List name is required")
    private String listName;
}
