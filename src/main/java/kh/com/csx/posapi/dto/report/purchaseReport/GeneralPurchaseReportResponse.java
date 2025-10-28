package kh.com.csx.posapi.dto.report.purchaseReport;

import com.fasterxml.jackson.annotation.JsonFormat;
import kh.com.csx.posapi.constant.Constant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralPurchaseReportResponse{

    private Long id;
    private Long purchaseOrderId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATETIME_FORMAT)
    private LocalDateTime date;

    private String referenceNo;
    private String poReferenceNo;
    private String biller;
    private String warehouse;
    private String supplier;
    private Double total;
    private Double shipping;
    private Double orderDiscount;
    private String orderDiscountId;
    private Double orderTax;
    private String orderTaxId;
    private Double grandTotal;
    private Double paid;
    private Double balance;
    private String status;
    private String paymentStatus;
    private String note;
    private String attachment;
    private String createdBy;
}


