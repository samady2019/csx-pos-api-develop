package kh.com.csx.posapi.dto.adjustment;

import kh.com.csx.posapi.entity.AdjustmentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustmentResponse {
    private AdjustmentEntity adjustment;
}
