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
public class TaxRateUpdateRequest {
    @NotNull(message = "Tax rate ID is required")
    private Long id;

    @NotBlank(message = "Tax code is required.")
    @Size(max = 50, message = "Tax code should not exceed {max} characters.")
    private String code;

    @NotBlank(message = "Tax name is required.")
    @Size(max = 50, message = "Tax name should not exceed {max} characters.")
    private String name;

    @NotNull(message = "Tax rate is required.")
    @PositiveOrZero(message = "Tax rate must be zero or a positive number.")
    private Double rate;
}
