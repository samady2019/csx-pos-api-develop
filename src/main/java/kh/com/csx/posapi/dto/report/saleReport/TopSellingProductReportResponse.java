package kh.com.csx.posapi.dto.report.saleReport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingProductReportResponse {

    private Long productId;
    private String productCode;
    private String productName;
    private String category;
    private String brand;
    private String unitCode;
    private String unitNameEn;
    private String unitNameKh;
    private Double quantitySold;
    private Double totalPriceAmount;
    private Double totalCostAmount;
    private Double totalProfit;
}
