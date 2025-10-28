package kh.com.csx.posapi.dto.report.inventoryReport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    public Long productId;
    public String image;
    public String productCode;
    public String barCode;
    public String productNameEn;
    public String productNameKh;
    public String category;
    public String brand;
    public String currency;
    public String unitCode;
    public String unitNameEn;
    public String unitNameKh;
    public String type;
    public String description;
    public String productDetails;

    @Builder
    public ProductResponse(Long productId, String productCode, String barCode, String productNameEn, String productNameKh, String category, String brand, String currency, String type, String description, String productDetails) {
        this.productId      = productId;
        this.productCode    = productCode;
        this.barCode        = barCode;
        this.productNameEn  = productNameEn;
        this.productNameKh  = productNameKh;
        this.category       = category;
        this.brand          = brand;
        this.currency       = currency;
        this.type           = type;
        this.description    = description;
        this.productDetails = productDetails;
    }
}
