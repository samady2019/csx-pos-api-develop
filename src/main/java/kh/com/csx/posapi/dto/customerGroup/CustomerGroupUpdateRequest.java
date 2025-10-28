package kh.com.csx.posapi.dto.customerGroup;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerGroupUpdateRequest {
    @NotNull(message = "Customer group ID is required.")
    private Long id;

    @NotBlank(message = "Name is required.")
    @Size(max = 255, message = "Name should not exceed {max} characters.")
    private String name;

    @NotNull(message = "Percentage is required.")
    @DecimalMin(value = "-100.0", inclusive = true, message = "Percentage must not exceed -100.")
    @DecimalMax(value = "100.0", inclusive = true, message = "Percentage must not exceed 100.")
    private Double percent;
}
