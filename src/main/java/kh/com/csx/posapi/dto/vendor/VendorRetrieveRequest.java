package kh.com.csx.posapi.dto.vendor;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorRetrieveRequest extends FilterDTO {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String shopNameEn;
    private String shopNameKh;
}
