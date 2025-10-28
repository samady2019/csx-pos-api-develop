package kh.com.csx.posapi.dto.unit;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnitRetrieveRequest extends FilterDTO {
    private Long unitId;
    private Long punitId;
    private String unitCode;
    private String unitNameEn;
    private String unitNameKh;
}
