package kh.com.csx.posapi.dto.biller;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillerRetrieveRequest extends FilterDTO {
    private Long id;
    private List<Long> ids;
    private String code;
    private String companyEn;
    private String companyKh;
    private String nameEn;
    private String nameKh;
    private String vatNo;
    private String contactPerson;
    private String phone;
    private String email;
}
