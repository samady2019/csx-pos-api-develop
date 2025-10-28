package kh.com.csx.posapi.dto.currencyCalender;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyCalenderCreateRequest {
    @NotNull(message = "Currency ID is required.")
    private Long currencyId;

    @NotNull(message = "Date is required.")
    private LocalDate date;

    @NotNull(message = "Rate is required.")
    @PositiveOrZero(message = "Rate must be zero or a positive number.")
    private Double rate;
}
