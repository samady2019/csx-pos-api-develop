package kh.com.csx.posapi.dto.taxRate;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaxRateProductUpdateRequest {
    @Valid
    @NotEmpty(message = "Product tax rate must contain at least one item.")
    private List<ProductTax> productsTax;
}
