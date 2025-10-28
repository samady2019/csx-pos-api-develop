package kh.com.csx.posapi.dto.report.inventoryReport;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrandResponse {
    private Long brandId;
    private String code;
    private String name;
    private Double purchaseQuantity;
    private Double purchaseAmount;
    private Double soldQuantity;
    private Double soldAmount;

    public BrandResponse(Tuple data) {
        this.brandId          = ((Number) data.get("brandId")).longValue();
        this.code             = (String) data.get("code");
        this.name             = (String) data.get("name");
        this.purchaseQuantity = ((Number) data.get("purchaseQuantity")).doubleValue();
        this.purchaseAmount   = ((Number) data.get("purchaseAmount")).doubleValue();
        this.soldQuantity     = ((Number) data.get("soldQuantity")).doubleValue();
        this.soldAmount       = ((Number) data.get("soldAmount")).doubleValue();
    }
}
