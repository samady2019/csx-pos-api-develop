package kh.com.csx.posapi.dto.report.purchaseReport;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralPurchaseReportRequest extends FilterDTO {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long purchaseOrderId;
    private String referenceNo;
    private String poReferenceNo;
    private Long billerId;
    private Long warehouseId;
    private Long supplierId;
    private Long productId;
    private String status;
    private String paymentStatus;
    private Long createdBy;
    private Long updatedBy;
}
