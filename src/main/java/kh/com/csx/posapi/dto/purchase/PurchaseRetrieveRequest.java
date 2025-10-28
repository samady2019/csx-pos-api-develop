package kh.com.csx.posapi.dto.purchase;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseRetrieveRequest extends FilterDTO {
    private Long id;
    private String referenceNo;
    private String poReferenceNo;
    private Long billerId;
    private Long warehouseId;
    private Long supplierId;
    private String status;
    private String paymentStatus;
    private Boolean taxDeclare;
    private Long createdBy;
    private Long updatedBy;
    private String startDate;
    private String endDate;
}
