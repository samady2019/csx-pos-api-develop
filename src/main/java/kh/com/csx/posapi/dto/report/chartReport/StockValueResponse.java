package kh.com.csx.posapi.dto.report.chartReport;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockValueResponse {
    private Long   totalItems;
    private Double totalQuantity;
    private Double stockValueByCost;
    private Double stockValueByPrice;
    private Double profitEstimate;

    public StockValueResponse(Tuple data) {
        this.totalItems        = ((Number) data.get("totalItems")).longValue();
        this.totalQuantity     = ((Number) data.get("totalQuantity")).doubleValue();
        this.stockValueByCost  = ((Number) data.get("stockValueByCost")).doubleValue();
        this.stockValueByPrice = ((Number) data.get("stockValueByPrice")).doubleValue();
        this.profitEstimate    = ((Number) data.get("profitEstimate")).doubleValue();
    }
}
