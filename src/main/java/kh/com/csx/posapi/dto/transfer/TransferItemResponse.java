package kh.com.csx.posapi.dto.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferItemResponse {
    private Long id;
    private Long transferId;
    private Long fromWarehouse;
    private Long toWarehouse;
    private Long productId;
    private LocalDateTime date;
    private Long unitId;
    private Double unitQuantity;
    private Double quantity;
    private Double unitCost;
    private Double baseUnitCost;
}
