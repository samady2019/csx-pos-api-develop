package kh.com.csx.posapi.dto.report.chartReport;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class OverviewRequest extends FilterDTO {
    private String month;
    private String year;
    private Long billerId;
    private Long warehouseId;
    private Long supplierId;
    private Long customerId;
    private Long createdBy;
    private Long updatedBy;
    private String startDate;
    private String endDate;
}
