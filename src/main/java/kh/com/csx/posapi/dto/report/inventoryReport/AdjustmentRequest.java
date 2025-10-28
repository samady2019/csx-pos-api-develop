package kh.com.csx.posapi.dto.report.inventoryReport;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class AdjustmentRequest extends ProductRequest {
    private Long id;
    private Long countId;
    private String referenceNo;
}
