package kh.com.csx.posapi.dto.report.chartReport;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockWarehouseResponse {
    private Long id;
    private String code;
    private String name;
    private Double totalQuantity;

    public StockWarehouseResponse(Tuple data) {
        this.id            = ((Number) data.get("id")).longValue();
        this.code          = (String) data.get("code");
        this.name          = (String) data.get("name");
        this.totalQuantity = ((Number) data.get("totalQuantity")).doubleValue();
    }
}
