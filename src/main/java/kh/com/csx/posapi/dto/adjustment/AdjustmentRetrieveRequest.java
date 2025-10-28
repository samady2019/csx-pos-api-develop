package kh.com.csx.posapi.dto.adjustment;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdjustmentRetrieveRequest extends FilterDTO {
    private Long id;
    private Long countId;
    private String referenceNo;
    private Long warehouseId;
    private Long billerId;
    private String startDate;
    private String endDate;
    private String note;
    private Long createdBy;
    private Long updatedBy;
}
