package kh.com.csx.posapi.dto.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
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
public class PaymentCreateRequest {
    private Long purchaseOrderId;
    private Long purchaseId;
    private Long purchaseReturnId;
    private Long saleOrderId;
    private Long saleId;
    private Long saleReturnId;
    private Long expenseId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATETIME_FORMAT)
    @NotNull(message = "Date is required.")
    private LocalDateTime date;

    @NotBlank(message = "Reference number is required.")
    @Size(max = 50, message = "Reference number should not exceed {max} characters.")
    private String referenceNo;

    @NotNull(message = "Payment method ID is required.")
    private Long paymentMethodId;

    @Size(max = 255, message = "Account number should not exceed {max} characters.")
    private String accountNumber;

    @Size(max = 255, message = "Account name should not exceed {max} characters.")
    private String accountName;

    @Size(max = 255, message = "Bank name should not exceed {max} characters.")
    private String bankName;

    @Size(max = 255, message = "Cheque No. should not exceed {max} characters.")
    private String chequeNo;

    @Size(max = 255, message = "Cheque date should not exceed {max} characters.")
    private String chequeDate;

    @Size(max = 255, message = "Cheque number should not exceed {max} characters.")
    private String chequeNumber;

    @Size(max = 255, message = "CC No. should not exceed {max} characters.")
    private String ccNo;

    @Size(max = 255, message = "CC CVV2 should not exceed {max} characters.")
    private String ccCvv2;

    @Size(max = 255, message = "CC Holder should not exceed {max} characters.")
    private String ccHolder;

    @Size(max = 255, message = "CC Month should not exceed {max} characters.")
    private String ccMonth;

    @Size(max = 255, message = "CC Year should not exceed {max} characters.")
    private String ccYear;

    @Size(max = 255, message = "CC Type should not exceed {max} characters.")
    private String ccType;

    @NotBlank(message = "Currencies is required.")
    private String currencies;

    private String note;

    @Size(max = 255, message = "Attachment should not exceed {max} characters.")
    private String attachment;
}
