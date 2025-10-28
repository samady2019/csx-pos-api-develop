package kh.com.csx.posapi.dto.stockCount;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockCountFinalRequest {
    @NotNull(message = "Stock count ID is required")
    private Long id;

    @NotEmpty(message = "Stock count finalize must contain at least one item")
    private List<StockCountItemRequest> items;
}
