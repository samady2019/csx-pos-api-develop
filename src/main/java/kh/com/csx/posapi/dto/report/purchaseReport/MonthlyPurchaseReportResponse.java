package kh.com.csx.posapi.dto.report.purchaseReport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyPurchaseReportResponse {

    private Integer year;
    private Integer month;
    private Double total;
    private Double discount;
    private Double productTax;
    private Double orderTax;
    private Double shipping;
    private Double grandTotal;
    private Double paid;
    private Integer totalPurchases;
}
