package kh.com.csx.posapi.dto.report.financialReport;

import kh.com.csx.posapi.constant.Constant.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseResponse {
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    private LocalDateTime date;

    private String referenceNo;
    private String biller;
    private String expenseCategory;
    private Double amount;
    private Double paid;
    private Double balance;
    private String attachment;
    private String note;
    private String paymentStatus;
    private String createdBy;

    public ExpenseResponse(Tuple data) {
        this.id              = ((Number) data.get("id")).longValue();
        this.date            = (LocalDateTime) data.get("date");
        this.referenceNo     = (String) data.get("referenceNo");
        this.biller          = (String) data.get("biller");
        this.expenseCategory = (String) data.get("expenseCategory");
        this.amount          = ((Number) data.get("amount")).doubleValue();
        this.paid            = ((Number) data.get("paid")).doubleValue();
        this.balance         = ((Number) data.get("balance")).doubleValue();
        this.attachment      = (String) data.get("attachment");
        this.note            = (String) data.get("note");
        this.paymentStatus   = (String) data.get("paymentStatus");
        this.createdBy       = (String) data.get("createdBy");
    }
}
