package kh.com.csx.posapi.dto.currencyCalender;

import kh.com.csx.posapi.entity.CurrencyCalenderEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyCalenderResponse {
    private CurrencyCalenderEntity currencyCalender;
}
