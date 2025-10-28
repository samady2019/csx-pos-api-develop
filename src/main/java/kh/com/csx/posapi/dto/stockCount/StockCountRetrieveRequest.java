package kh.com.csx.posapi.dto.stockCount;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockCountRetrieveRequest extends FilterDTO {
    private Long id;
    private String referenceNo;
    private Long billerId;
    private Long warehouseId;
    private String type;
    private String status;
    private Long createdBy;
    private Long updatedBy;
    private String startDate;
    private String endDate;
}
