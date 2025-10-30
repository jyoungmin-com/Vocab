package jyoungmin.vocablist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {
    private Long id;
    private String userName;
    private String name;
    private String email;
    private String role;
    private boolean enabled;
}
