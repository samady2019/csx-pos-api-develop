package kh.com.csx.posapi.dto.paymentMethod;

import kh.com.csx.posapi.entity.PaymentMethodEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodResponse {
    private PaymentMethodEntity paymentMethod;
}
