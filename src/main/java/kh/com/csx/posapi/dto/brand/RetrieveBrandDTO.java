package kh.com.csx.posapi.dto.brand;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RetrieveBrandDTO extends FilterDTO {
    private Long brandId;
    private String code;
    private String name;
    private Integer status;
}
