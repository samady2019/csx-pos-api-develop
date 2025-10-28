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
public class LanguageDeleteRequest {
    @NotNull(message = "Language ID is required.")
    private Object id;
}
