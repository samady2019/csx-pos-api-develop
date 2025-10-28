package kh.com.csx.posapi.dto.report.saleReport;

import com.fasterxml.jackson.annotation.JsonFormat;
import kh.com.csx.posapi.constant.Constant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesDiscountReportResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATETIME_FORMAT)
    private LocalDateTime date;
    private String referenceNo;
    private String Customer;
    private Long productId;
    private String ProductCode;
    private String ProductNameEn;
    private String ProductNameKh;
    private Double unitPrice;
    private Double unitQuantity;
    private String discount;
}