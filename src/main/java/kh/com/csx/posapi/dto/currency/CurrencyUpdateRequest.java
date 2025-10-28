package kh.com.csx.posapi.dto.currency;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyUpdateRequest {
    @NotNull(message = "Currency ID is required")
    private Long id;

    @NotBlank(message = "Currency code is required")
    @Size(max = 50, message = "Currency code should not exceed {max} characters.")
    private String code;

    @NotBlank(message = "Currency name is required")
    @Size(max = 50, message = "Currency name should not exceed {max} characters.")
    private String name;

    @NotBlank(message = "Currency symbol is required")
    @Size(max = 50, message = "Currency symbol should not exceed {max} characters.")
    private String symbol;

    @NotNull(message = "Rate is required")
    @Positive(message = "Rate must be a positive number.")
    private Double rate;
}
