package kh.com.csx.posapi.dto.purchaseOrder;

import kh.com.csx.posapi.entity.PurchaseOrderEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderResponse {
    private PurchaseOrderEntity purchaseOrder;
}
