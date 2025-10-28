package kh.com.csx.posapi.dto.adjustment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustmentItemResponse {
    private Long id;
    private Long productId;
    private Long unitId;
    private Double unitQuantity;
    private Double quantity;
    private Double unitCost;
    private Double baseUnitCost;
    private String type;
    private LocalDateTime expiry;
}

