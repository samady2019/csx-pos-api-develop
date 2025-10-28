package kh.com.csx.posapi.dto.report.inventoryReport;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    private Long categoryId;
    private String code;
    private String name;
    private Double purchaseQuantity;
    private Double purchaseAmount;
    private Double soldQuantity;
    private Double soldAmount;

    public CategoryResponse(Tuple data) {
        this.categoryId       = ((Number) data.get("categoryId")).longValue();
        this.code             = (String) data.get("code");
        this.name             = (String) data.get("name");
        this.purchaseQuantity = ((Number) data.get("purchaseQuantity")).doubleValue();
        this.purchaseAmount   = ((Number) data.get("purchaseAmount")).doubleValue();
        this.soldQuantity     = ((Number) data.get("soldQuantity")).doubleValue();
        this.soldAmount       = ((Number) data.get("soldAmount")).doubleValue();
    }
}
