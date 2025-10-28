package kh.com.csx.posapi.dto.transfer;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferRetrieveRequest extends FilterDTO {
    private Long id;
    private String referenceNo;
    private Long billerId;
    private Long warehouseId;
    private String status;
    private String startDate;
    private String endDate;
    private Long createdBy;
    private Long updatedBy;
}
