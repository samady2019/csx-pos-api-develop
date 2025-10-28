package kh.com.csx.posapi.dto.currencyCalender;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyCalenderDeleteRequest {
    @NotNull(message = "Currency rate ID is required.")
    private Object id;
}
