package kh.com.csx.posapi.dto.purchase;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import kh.com.csx.posapi.constant.Constant.DateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseImportRequest {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    @NotNull(message = "Date is required.")
    private String date;

    @NotBlank(message = "Reference number is required.")
    @Size(max = 50, message = "Reference number should not exceed {max} characters.")
    private String referenceNo;

    @NotNull(message = "Biller ID is required.")
    private Long billerId;

    @NotNull(message = "Warehouse ID is required.")
    private Long warehouseId;

    @NotNull(message = "Supplier ID is required.")
    private Long supplierId;

    @NotNull(message = "Shipping is required.")
    @PositiveOrZero(message = "Shipping must be zero or positive.")
    private Double shipping;

    @Size(max = 50, message = "Order discount should not exceed {max} characters.")
    private String orderDiscountId;

    @NotNull(message = "Tax rate is required.")
    private Long orderTaxId;

    @Positive(message = "Payment term must be greater than zero.")
    private Long paymentTerm;

    private String currencies;

    private MultipartFile attachment;

    private String staffNote;

    private String note;

    @NotBlank(message = "Status is required.")
    @Size(max = 50, message = "Status should not exceed {max} characters.")
    private String status;

    @NotNull(message = "File is required.")
    private MultipartFile file;
}
