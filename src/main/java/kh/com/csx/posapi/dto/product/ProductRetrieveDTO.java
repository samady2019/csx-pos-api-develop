package kh.com.csx.posapi.dto.product;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRetrieveDTO extends FilterDTO {
    private Long productId;
    private String productCode;
    private String barCode;
    private String productNameEn;
    private String productNameKh;
    private Long categoryId;
    private Long brandId;
    private String type;
    private Integer status;
    private Long warehouseId;
    private Boolean standard;
    private Boolean inStockOnly;
}
