package kh.com.csx.posapi.dto.report.financialReport;

import com.fasterxml.jackson.annotation.JsonInclude;
import kh.com.csx.posapi.constant.Constant.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentAlertResponse {
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    private LocalDateTime date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATE_FORMAT)
    private LocalDate dueDate;

    private String referenceNo;
    private String biller;
    private String warehouse;
    private String supplier;
    private String customer;
    private Double grandTotal;
    private Double paid;
    private Double balance;
    private String status;
    private String paymentStatus;
    private String attachment;
    private String note;
    private String createdBy;

    public PaymentAlertResponse(Tuple data) {
        this.id            = ((Number) data.get("id")).longValue();
        this.date          = (LocalDateTime) data.get("date");
        this.dueDate       = (LocalDate) data.get("dueDate");
        this.referenceNo   = (String) data.get("referenceNo");
        this.biller        = (String) data.get("biller");
        this.warehouse     = (String) data.get("warehouse");
        this.supplier      = (String) data.get("supplier");
        this.customer      = (String) data.get("customer");
        this.grandTotal    = ((Number) data.get("grandTotal")).doubleValue();
        this.paid          = ((Number) data.get("paid")).doubleValue();
        this.balance       = ((Number) data.get("balance")).doubleValue();
        this.status        = (String) data.get("status");
        this.paymentStatus = (String) data.get("paymentStatus");
        this.attachment    = (String) data.get("attachment");
        this.note          = (String) data.get("note");
        this.createdBy     = (String) data.get("createdBy");
    }
}
