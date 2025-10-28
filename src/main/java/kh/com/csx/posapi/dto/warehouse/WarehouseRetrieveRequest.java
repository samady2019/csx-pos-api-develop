package kh.com.csx.posapi.dto.warehouse;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseRetrieveRequest extends FilterDTO {
    private Long id;
    private String code;
    private String name;
    private String fax;
    private String phone;
    private String email;
    private Integer overselling;
}
