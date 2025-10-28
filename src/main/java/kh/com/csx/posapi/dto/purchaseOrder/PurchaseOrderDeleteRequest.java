package kh.com.csx.posapi.dto.purchaseOrder;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderDeleteRequest {
    @NotNull(message = "Purchase order ID is required.")
    private Object id;
}
