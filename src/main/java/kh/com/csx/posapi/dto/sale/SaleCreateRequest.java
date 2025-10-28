package kh.com.csx.posapi.dto.sale;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
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
public class SaleCreateRequest {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    @NotNull(message = "Date is required.")
    private LocalDateTime date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    private LocalDateTime orderDate;

    private Long saleOrderId;
    private Long quoteId;

    @Size(max = 50, message = "Reference number should not exceed {max} characters.")
    private String referenceNo;

    @Size(max = 255, message = "PO number should not exceed {max} characters.")
    private String poNumber;

    @NotNull(message = "Biller ID is required.")
    private Long billerId;

    @NotNull(message = "Warehouse ID is required.")
    private Long warehouseId;

    @NotNull(message = "Customer ID is required.")
    private Long customerId;

    @NotNull(message = "Shipping is required.")
    @PositiveOrZero(message = "Shipping must be zero or positive")
    private Double shipping;

    @Size(max = 50, message = "Order discount should not exceed {max} characters.")
    private String orderDiscountId;

    @NotNull(message = "Tax rate is required.")
    private Long orderTaxId;

    @Positive(message = "Payment term must be greater than zero")
    private Long paymentTerm;

    private String currencies;

    @Size(max = 255, message = "Attachment should not exceed {max} characters.")
    private String attachment;

    private Long suspendId;

    private String staffNote;

    private String note;

    private Integer pos;

    @Size(max = 50, message = "Status should not exceed {max} characters.")
    private String status;

    @Size(max = 50, message = "Delivery status should not exceed {max} characters.")
    private String deliveryStatus;

    private Long salesmanBy;

    @Valid
    @NotEmpty(message = "Sale must contain at least one item")
    private List<SaleItemRequest> items;

    private List<PaymentCreateRequest> payments;
}
