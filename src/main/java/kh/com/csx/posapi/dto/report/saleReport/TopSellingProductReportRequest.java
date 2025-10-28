package kh.com.csx.posapi.dto.report.saleReport;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingProductReportRequest extends FilterDTO {
    private Long productId;
    private String productCode;
    private String barCode;
    private String productNameEn;
    private String productNameKh;
    private Long categoryId;
    private Long brandId;
    private String type;
    private Long billerId;
    private Long warehouseId;
    private Long createdBy;
    private Long updatedBy;
    private LocalDate startDate;
    private LocalDate endDate;
}
