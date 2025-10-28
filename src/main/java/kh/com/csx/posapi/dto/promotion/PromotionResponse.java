package kh.com.csx.posapi.dto.promotion;

import kh.com.csx.posapi.entity.PromotionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionResponse {
    private PromotionEntity promotion;
}
