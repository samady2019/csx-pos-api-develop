package kh.com.csx.posapi.dto.report.financialReport;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class ExpenseRequest extends PaymentRequest {
    private Long id;
    private Long expenseCategoryId;
    private String paymentStatus;
}
