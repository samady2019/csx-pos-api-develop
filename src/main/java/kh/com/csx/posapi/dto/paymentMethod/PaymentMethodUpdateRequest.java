package kh.com.csx.posapi.dto.paymentMethod;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodUpdateRequest {
    @NotNull(message = "Payment method ID is required")
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name should not exceed {max} characters.")
    private String name;

    private String description;

    @NotBlank(message = "Type is required")
    @Size(max = 255, message = "Type should not exceed {max} characters.")
    private String type;

    private Integer status;
}
