package kh.com.csx.posapi.dto.report.saleReport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailySalesReportResponse {
    private LocalDate date;
    private double total;
    private double discount;
    private double productTax;
    private double orderTax;
    private double shipping;
    private double grandTotal;
    private double paid;
    private long totalSales;
}
