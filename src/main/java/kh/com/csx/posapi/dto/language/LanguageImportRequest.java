package kh.com.csx.posapi.dto.language;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageImportRequest {
    @NotNull(message = "File is required.")
    private MultipartFile file;
}
