package kh.com.csx.posapi.dto.report.financialReport;

import com.fasterxml.jackson.annotation.JsonFormat;
import kh.com.csx.posapi.constant.Constant.DateTime;
import java.time.LocalDateTime;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long purchaseOrderId;
    private Long purchaseId;
    private Long saleOrderId;
    private Long saleId;
    private Long expenseId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    private LocalDateTime date;

    private String referenceNo;
    private String poRefNo;
    private String puRefNo;
    private String soRefNo;
    private String slRefNo;
    private String expRefNo;
    private String biller;
    private String supplier;
    private String customer;
    private String paymentMethod;
    private Double amount;
    private String attachment;
    private String note;
    private String type;
    private String createdBy;

    public PaymentResponse(Tuple data) {
        this.id              = ((Number) data.get("id")).longValue();
        this.purchaseOrderId = data.get("purchaseOrderId") != null ? ((Number) data.get("purchaseOrderId")).longValue() : null;
        this.purchaseId      = data.get("purchaseId") != null ? ((Number) data.get("purchaseId")).longValue() : null;
        this.saleOrderId     = data.get("saleOrderId") != null ? ((Number) data.get("saleOrderId")).longValue() : null;
        this.saleId          = data.get("saleId") != null ? ((Number) data.get("saleId")).longValue() : null;
        this.expenseId       = data.get("expenseId") != null ? ((Number) data.get("expenseId")).longValue() : null;
        this.date            = (LocalDateTime) data.get("date");
        this.referenceNo     = (String) data.get("referenceNo");
        this.poRefNo         = (String) data.get("poRefNo");
        this.puRefNo         = (String) data.get("puRefNo");
        this.soRefNo         = (String) data.get("soRefNo");
        this.slRefNo         = (String) data.get("slRefNo");
        this.expRefNo        = (String) data.get("expRefNo");
        this.biller          = (String) data.get("biller");
        this.supplier        = (String) data.get("supplier");
        this.customer        = (String) data.get("customer");
        this.paymentMethod   = (String) data.get("paymentMethod");
        this.amount          = ((Number) data.get("amount")).doubleValue();
        this.attachment      = (String) data.get("attachment");
        this.note            = (String) data.get("note");
        this.type            = (String) data.get("type");
        this.createdBy       = (String) data.get("createdBy");
    }
}
