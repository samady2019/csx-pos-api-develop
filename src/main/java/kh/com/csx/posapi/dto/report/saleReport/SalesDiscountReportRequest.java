package kh.com.csx.posapi.dto.report.saleReport;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesDiscountReportRequest extends FilterDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long productId;
    private Long billerId;
    private Long warehouseId;
    private Long customerId;
    private String referenceNo;
}
