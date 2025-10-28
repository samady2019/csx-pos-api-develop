package kh.com.csx.posapi.dto.sale;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaleRetrieveRequest extends FilterDTO {
    private Long id;
    private List<Long> ids;
    private String referenceNo;
    private String soReferenceNo;
    private String qtReferenceNo;
    private Long billerId;
    private Long warehouseId;
    private Long customerId;
    private String status;
    private String paymentStatus;
    private String deliveryStatus;
    private Long salesmanBy;
    private Boolean taxDeclare;
    private Long createdBy;
    private Long updatedBy;
    private String startDate;
    private String endDate;
    private Integer pos;
}
