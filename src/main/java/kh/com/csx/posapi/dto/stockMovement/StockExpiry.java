package kh.com.csx.posapi.dto.stockMovement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockExpiry {
    private Long productId;
    private LocalDate expiry;
    private Double quantity;
}
