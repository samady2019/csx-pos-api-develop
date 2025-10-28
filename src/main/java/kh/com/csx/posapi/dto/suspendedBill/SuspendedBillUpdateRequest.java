package kh.com.csx.posapi.dto.suspendedBill;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuspendedBillUpdateRequest {
    @NotNull(message = "Suspend ID is required.")
    private Long id;

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

    @Size(max = 255, message = "Attachment should not exceed {max} characters.")
    private String attachment;

    private String suspendNote;

    private String staffNote;

    private String note;

    private Long salesmanBy;

    @Valid
    @NotEmpty(message = "Suspend must contain at least one item")
    private List<SuspendedItemRequest> items;
}
