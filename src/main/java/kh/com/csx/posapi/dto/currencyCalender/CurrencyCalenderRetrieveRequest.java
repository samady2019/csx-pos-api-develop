package kh.com.csx.posapi.dto.currencyCalender;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyCalenderRetrieveRequest extends FilterDTO {
    private Long id;
    private Long currencyId;
    private LocalDate date;
    private String code;
    private String name;
    private String symbol;
}
