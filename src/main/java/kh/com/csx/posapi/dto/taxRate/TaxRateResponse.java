package kh.com.csx.posapi.dto.taxRate;

import kh.com.csx.posapi.entity.TaxRateEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaxRateResponse {
    private TaxRateEntity taxRate;
}
