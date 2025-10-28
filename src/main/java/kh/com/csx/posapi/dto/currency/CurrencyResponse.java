package kh.com.csx.posapi.dto.currency;

import kh.com.csx.posapi.entity.CurrencyEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyResponse {
    private CurrencyEntity currency;
}
