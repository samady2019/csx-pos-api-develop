package kh.com.csx.posapi.dto.stockMovement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockCost {
    private Double cost;
    private Double quantity;
    private Double outQuantity;
}
