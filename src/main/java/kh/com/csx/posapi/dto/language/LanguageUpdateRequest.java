package kh.com.csx.posapi.dto.language;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LanguageUpdateRequest {
    @NotNull(message = "Language ID is required.")
    private Long id;

    @NotBlank(message = "Code is required.")
    @Size(max = 255, message = "Code should not exceed {max} characters.")
    private String code;

    private String khmer;
    private String english;
    private String chinese;
    private String thai;
    private String vietnamese;
}
