package kh.com.csx.posapi.dto.report.saleReport;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralSalesReportRequest extends FilterDTO {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long saleOrderId;
    private String referenceNo;
    private String soReferenceNo;
    private Long billerId;
    private Long warehouseId;
    private Long customerId;
    private Long productId;
    private String status;
    private String paymentStatus;
    private String deliveryStatus;
    private Long createdBy;
    private Long updatedBy;
}
