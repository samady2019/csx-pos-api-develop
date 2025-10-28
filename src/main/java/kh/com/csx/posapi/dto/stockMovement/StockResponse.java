package kh.com.csx.posapi.dto.stockMovement;

import kh.com.csx.posapi.entity.WarehouseProductEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockResponse {
    private Double quantity;
    private List<WarehouseProductEntity> warehousesProduct;
    private List<StockExpiry> expiriesProduct;
}
