package kh.com.csx.posapi.dto.report.inventoryReport;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProductRequest extends FilterDTO {
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
    private String startDate;
    private String endDate;
    private Long createdBy;
    private Long updatedBy;
    private Integer all = 1;
}
