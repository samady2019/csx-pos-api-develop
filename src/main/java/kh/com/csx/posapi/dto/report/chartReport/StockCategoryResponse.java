package kh.com.csx.posapi.dto.report.chartReport;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockCategoryResponse {
    private Long categoryId;
    private String code;
    private String name;
    private Double totalQuantity;

    public StockCategoryResponse(Tuple data) {
        this.categoryId    = ((Number) data.get("categoryId")).longValue();
        this.code          = (String) data.get("code");
        this.name          = (String) data.get("name");
        this.totalQuantity = ((Number) data.get("totalQuantity")).doubleValue();
    }
}
