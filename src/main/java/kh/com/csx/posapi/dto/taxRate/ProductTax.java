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
public class ProductTax {
    @NotNull(message = "Product ID is required.")
    private Long productId;

    @Min(value = 0, message = "Default tax rate declare must be at least 0.")
    @Max(value = 100, message = "Default tax rate declare must not exceed 100.")
    private Double taxRateDeclare;
}
