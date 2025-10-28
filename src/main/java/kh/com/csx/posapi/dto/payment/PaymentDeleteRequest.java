package kh.com.csx.posapi.dto.payment;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDeleteRequest {
    @NotNull(message = "Payment ID is required")
    private Long id;
}
