package kh.com.csx.posapi.dto.supplier;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierRetrieveRequest extends FilterDTO {
    private Long id;
    private String companyEn;
    private String companyKh;
    private String nameEn;
    private String nameKh;
    private String gender;
    private String vatNo;
    private String contactPerson;
    private String phone;
    private String email;
}
