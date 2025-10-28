package kh.com.csx.posapi.dto.taxRate;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaxDeclareItemRequest {
    @NotNull(message = "Transaction ID is required.")
    private Long transactionId;

    @PositiveOrZero(message = "Exchange rate must be zero or positive.")
    private Double exchangeRate;
}
