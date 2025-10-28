package kh.com.csx.posapi.dto.report.inventoryReport;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Tuple;
import kh.com.csx.posapi.constant.Constant.DateTime;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WarehouseResponse extends ProductResponse {
    private List<Warehouse> warehouses;

    @Data
    @NoArgsConstructor
    public static class Warehouse {
        private Long id;
        private String code;
        private String name;

        @JsonInclude(JsonInclude.Include.ALWAYS)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATE_FORMAT)
        private LocalDate expiry;

        private Double quantity;

        public Warehouse(Long id, String code, String name, LocalDate expiry, Double quantity) {
            this.id       = id;
            this.code     = code;
            this.name     = name;
            this.expiry   = expiry;
            this.quantity = quantity;
        }
    }

    public WarehouseResponse(Tuple product, List<Tuple> warehousesProduct) {
        this.productId     = ((Number) product.get("productId")).longValue();
        this.productCode   = (String) product.get("productCode");
        this.barCode       = (String) product.get("barCode");
        this.productNameEn = (String) product.get("productNameEn");
        this.productNameKh = (String) product.get("productNameKh");
        this.category      = (String) product.get("category");
        this.brand         = (String) product.get("brand");
        this.unitCode      = (String) product.get("unitCode");
        this.unitNameEn    = (String) product.get("unitNameEn");
        this.unitNameKh    = (String) product.get("unitNameKh");
        this.warehouses    = new ArrayList<>();
        for (Tuple wp : warehousesProduct) {
            Warehouse warehouse = new Warehouse(
                ((Number) wp.get("warehouseId")).longValue(),
                (String) wp.get("warehouseCode"),
                (String) wp.get("warehouseName"),
                (LocalDate) wp.get("expiry"),
                (wp.get("quantity") != null ? ((Number) wp.get("quantity")).doubleValue() : 0.0)
            );
            this.warehouses.add(warehouse);
        }
    }
}
