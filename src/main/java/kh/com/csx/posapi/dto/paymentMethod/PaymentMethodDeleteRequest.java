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
public class PaymentMethodDeleteRequest {
    @NotNull(message = "Payment method ID is required.")
    private Object id;
}
