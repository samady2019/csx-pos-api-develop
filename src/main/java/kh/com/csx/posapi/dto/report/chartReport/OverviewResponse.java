package kh.com.csx.posapi.dto.report.chartReport;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OverviewResponse {
    private String month;
    private String year;
    private Sale sale;
    private Purchase purchase;

    @Data
    @NoArgsConstructor
    private static class Sale {
        private Double total;
        private Double discount;
        private Double productTax;
        private Double orderTax;
        private Double shipping;
        private Double grandTotal;

        public Sale(Double total, Double discount, Double productTax, Double orderTax, Double shipping, Double grandTotal) {
            this.total      = total;
            this.discount   = discount;
            this.productTax = productTax;
            this.orderTax   = orderTax;
            this.shipping   = shipping;
            this.grandTotal = grandTotal;
        }
    }

    @Data
    @NoArgsConstructor
    private static class Purchase {
        private Double total;
        private Double discount;
        private Double productTax;
        private Double orderTax;
        private Double shipping;
        private Double grandTotal;

        public Purchase(Double total, Double discount, Double productTax, Double orderTax, Double shipping, Double grandTotal) {
            this.total      = total;
            this.discount   = discount;
            this.productTax = productTax;
            this.orderTax   = orderTax;
            this.shipping   = shipping;
            this.grandTotal = grandTotal;
        }
    }

    public OverviewResponse(Tuple data, Tuple dataSale, Tuple dataPurchase) {
        this.year  = (String) data.get("year");
        this.month = (String) data.get("month");
        this.sale  = new Sale(
            ((Number) dataSale.get("total")).doubleValue(),
            ((Number) dataSale.get("discount")).doubleValue(),
            ((Number) dataSale.get("productTax")).doubleValue(),
            ((Number) dataSale.get("orderTax")).doubleValue(),
            ((Number) dataSale.get("shipping")).doubleValue(),
            ((Number) dataSale.get("grandTotal")).doubleValue()
        );
        this.purchase  = new Purchase(
            ((Number) dataPurchase.get("total")).doubleValue(),
            ((Number) dataPurchase.get("discount")).doubleValue(),
            ((Number) dataPurchase.get("productTax")).doubleValue(),
            ((Number) dataPurchase.get("orderTax")).doubleValue(),
            ((Number) dataPurchase.get("shipping")).doubleValue(),
            ((Number) dataPurchase.get("grandTotal")).doubleValue()
        );
    }
}
