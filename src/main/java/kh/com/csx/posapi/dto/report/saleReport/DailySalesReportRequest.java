package kh.com.csx.posapi.dto.report.saleReport;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailySalesReportRequest extends FilterDTO {
    private Integer year;
    private Integer month;
    private Long billerId;
    private Long warehouseId;
    private Long createdBy;
    private Long updatedBy;
}
