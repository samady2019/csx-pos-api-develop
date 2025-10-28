package kh.com.csx.posapi.dto.stockCount;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockCountDeleteRequest {
    @NotNull(message = "Stock count ID is required.")
    private Object id;
}
