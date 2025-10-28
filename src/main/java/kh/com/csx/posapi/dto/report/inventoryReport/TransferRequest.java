package kh.com.csx.posapi.dto.report.inventoryReport;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class TransferRequest extends ProductRequest {
    private Long id;
    private String referenceNo;
    private String status;
}
