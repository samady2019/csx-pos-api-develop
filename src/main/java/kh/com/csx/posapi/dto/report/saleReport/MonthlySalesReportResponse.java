package kh.com.csx.posapi.dto.report.saleReport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlySalesReportResponse {
    private int year;
    private int month;
    private double total;
    private double discount;
    private double productTax;
    private double orderTax;
    private double shipping;
    private double grandTotal;
    private double paid;
    private long totalSales;
}