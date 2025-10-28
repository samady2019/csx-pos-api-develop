package kh.com.csx.posapi.dto.language;

import kh.com.csx.posapi.entity.LanguageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LanguageResponse {
    private LanguageEntity language;
}
