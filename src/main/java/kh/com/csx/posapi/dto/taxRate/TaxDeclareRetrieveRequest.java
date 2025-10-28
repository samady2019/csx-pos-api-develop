package kh.com.csx.posapi.dto.taxRate;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaxDeclareRetrieveRequest extends FilterDTO {
    private Long id;
    private Long tranId;
    private List<Long> tranIds;
    private String type;
    private Long billerId;
    private String month;
    private String year;
    private Long createdBy;
    private Long updatedBy;
    private String startDate;
    private String endDate;
}
