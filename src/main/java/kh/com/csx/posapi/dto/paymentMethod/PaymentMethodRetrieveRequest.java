package kh.com.csx.posapi.dto.paymentMethod;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodRetrieveRequest extends FilterDTO {
    private Long id;
    private String name;
    private String type;
    private Integer status;
}
