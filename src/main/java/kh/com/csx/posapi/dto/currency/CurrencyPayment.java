package kh.com.csx.posapi.dto.currency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyPayment {
    private String code;
    private Double rate;
    private Double amount;
}
