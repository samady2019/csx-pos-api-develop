package kh.com.csx.posapi.dto.promotion;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionRetrieveRequest extends FilterDTO {
    private Long id;
    private String name;
    private Long billerId;
    private Long productId;
}
