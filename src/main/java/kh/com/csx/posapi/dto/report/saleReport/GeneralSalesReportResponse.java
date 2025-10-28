package kh.com.csx.posapi.dto.report.saleReport;

import com.fasterxml.jackson.annotation.JsonFormat;
import kh.com.csx.posapi.constant.Constant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralSalesReportResponse {
    private Long id;
    private Long saleOrderId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATETIME_FORMAT)
    private LocalDateTime date;
    private String referenceNo;
    private String soReferenceNo;
    private String biller;
    private String warehouse;
    private String customer;
    private Double total;
    private Double shipping;
    private Double orderDiscount;
    private String orderDiscountId;
    private Double orderTax;
    private String orderTaxId;
    private Double grandTotal;
    private Double paid;
    private Double balance;
    private Double changes;
    private Double cost;
    private String status;
    private String paymentStatus;
    private String deliveryStatus;
    private String note;
    private String attachment;
    private String createdBy;
}
