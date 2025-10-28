package kh.com.csx.posapi.dto.report.inventoryReport;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Tuple;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductQuantityAlertResponse extends ProductResponse {
    private Double quantity;
    private Double alertQuantity;

    public ProductQuantityAlertResponse(Tuple data) {
        this.productId     = ((Number) data.get("productId")).longValue();
        this.productCode   = (String) data.get("productCode");
        this.barCode       = (String) data.get("barCode");
        this.productNameEn = (String) data.get("productNameEn");
        this.productNameKh = (String) data.get("productNameKh");
        this.category      = (String) data.get("category");
        this.brand         = (String) data.get("brand");
        this.unitCode      = (String) data.get("unitCode");
        this.unitNameEn    = (String) data.get("unitNameEn");
        this.unitNameKh    = (String) data.get("unitNameKh");
        this.quantity      = data.get("quantity") != null ? ((Number) data.get("quantity")).doubleValue() : 0.0;
        this.alertQuantity = data.get("alertQuantity") != null ? ((Number) data.get("alertQuantity")).doubleValue() : 0.0;
    }
}
