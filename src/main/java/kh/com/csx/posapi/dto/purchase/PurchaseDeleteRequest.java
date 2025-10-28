package kh.com.csx.posapi.dto.purchase;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseDeleteRequest {
    @NotNull(message = "Purchase ID is required.")
    private Object id;
}
