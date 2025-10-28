package kh.com.csx.posapi.dto.currency;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyRetrieveRequest extends FilterDTO {
    private Long id;
    private String code;
    private String name;
    private String symbol;
}
