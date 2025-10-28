package kh.com.csx.posapi.dto.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import kh.com.csx.posapi.constant.Constant.DateTime;
import kh.com.csx.posapi.dto.payment.PaymentCreateRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseCreateRequest {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    @NotNull(message = "Date is required.")
    private LocalDateTime date;

    @NotBlank(message = "Reference number is required.")
    @Size(max = 50, message = "Reference number should not exceed {max} characters.")
    private String referenceNo;

    @NotNull(message = "Biller ID is required.")
    private Long billerId;

    @NotNull(message = "Expense Category ID is required.")
    private Long expenseCategoryId;

    @NotNull(message = "Amount is required.")
    @Positive(message = "Amount must be greater than zero.")
    private Double amount;

    private String currencies;

    @Size(max = 255, message = "Attachment should not exceed {max} characters.")
    private String attachment;

    @Size(max = 500, message = "Note should not exceed {max} characters.")
    private String note;

    private List<PaymentCreateRequest> payments;
}
