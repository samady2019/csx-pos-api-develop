package kh.com.csx.posapi.dto.taxRate;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaxRateProductRetrieveRequest extends FilterDTO {
    private Long productId;
    private Long categoryId;
    private Long brandId;
    private String type;
}
