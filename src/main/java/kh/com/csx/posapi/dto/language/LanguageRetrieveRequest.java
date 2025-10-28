package kh.com.csx.posapi.dto.language;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LanguageRetrieveRequest extends FilterDTO {
    private Long id;
    private String langCode;
    private String code;
}
