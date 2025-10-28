package kh.com.csx.posapi.dto.report.inventoryReport;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Tuple;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductInOutResponse extends ProductResponse {
    private Double beginning;
    private Double purchase;
    private Double transferIn;
    private Double transferOut;
    private Double adjustmentAdd;
    private Double adjustmentSub;
    private Double sold;
    private Double balance;

    public ProductInOutResponse(Tuple data) {
        this.productId      = ((Number) data.get("productId")).longValue();
        this.productCode    = (String) data.get("productCode");
        this.barCode        = (String) data.get("barCode");
        this.productNameEn  = (String) data.get("productNameEn");
        this.productNameKh  = (String) data.get("productNameKh");
        this.category       = (String) data.get("category");
        this.brand          = (String) data.get("brand");
        this.unitCode       = (String) data.get("unitCode");
        this.unitNameEn     = (String) data.get("unitNameEn");
        this.unitNameKh     = (String) data.get("unitNameKh");
        this.beginning      = data.get("beginning")     != null ? ((Number) data.get("beginning")).doubleValue()     : 0.0;
        this.purchase       = data.get("purchase")      != null ? ((Number) data.get("purchase")).doubleValue()      : 0.0;
        this.transferIn     = data.get("transferIn")    != null ? ((Number) data.get("transferIn")).doubleValue()    : 0.0;
        this.transferOut    = data.get("transferOut")   != null ? ((Number) data.get("transferOut")).doubleValue()   : 0.0;
        this.adjustmentAdd  = data.get("adjustmentAdd") != null ? ((Number) data.get("adjustmentAdd")).doubleValue() : 0.0;
        this.adjustmentSub  = data.get("adjustmentSub") != null ? ((Number) data.get("adjustmentSub")).doubleValue() : 0.0;
        this.sold           = data.get("sold")          != null ? ((Number) data.get("sold")).doubleValue()          : 0.0;
        this.balance        = data.get("balance")       != null ? ((Number) data.get("balance")).doubleValue()       : 0.0;
    }
}
