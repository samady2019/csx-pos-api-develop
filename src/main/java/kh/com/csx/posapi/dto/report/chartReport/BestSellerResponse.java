package kh.com.csx.posapi.dto.report.chartReport;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class BestSellerResponse {
    private String month;
    private String year;
    private List<Product> products;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Product {
        private Long productId;
        private String productCode;
        private String barCode;
        private String productNameEn;
        private String productNameKh;
        private String unitCode;
        private String unitNameEn;
        private String unitNameKh;
        private Double totalQuantity;

        public Product(Tuple data) {
            this.productId     = ((Number) data.get("productId")).longValue();
            this.productCode   = (String) data.get("productCode");
            this.barCode       = (String) data.get("barCode");
            this.productNameEn = (String) data.get("productNameEn");
            this.productNameKh = (String) data.get("productNameKh");
            this.unitCode      = (String) data.get("unitCode");
            this.unitNameEn    = (String) data.get("unitNameEn");
            this.unitNameKh    = (String) data.get("unitNameKh");
            this.totalQuantity = ((Number) data.get("totalQuantity")).doubleValue();
        }
    }

    public BestSellerResponse(String month, String year, List<Tuple> data) {
        this.month    = month;
        this.year     = year;
        this.products = data.stream().map(Product::new).toList();
    }
}
