package kh.com.csx.posapi.dto.suspendedBill;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuspendedBillRetrieveRequest extends FilterDTO {
    private Long id;
    private String referenceNo;
    private Long billerId;
    private Long warehouseId;
    private Long customerId;
    private Integer status;
    private Long salesmanBy;
    private Long createdBy;
    private Long updatedBy;
    private String startDate;
    private String endDate;
}
