package kh.com.csx.posapi.dto.purchase;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import kh.com.csx.posapi.constant.Constant.DateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseItemRequest {
    private Long purchaseItemId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATE_FORMAT)
    private LocalDate expiry;

    @NotNull(message = "Unit ID is required.")
    private Long unitId;

    @NotNull(message = "Unit quantity is required.")
    @Positive(message = "Unit quantity must be positive")
    private Double unitQuantity;

    @NotNull(message = "Unit cost is required.")
    @PositiveOrZero(message = "Unit cost be zero or positive")
    private Double unitCost;

    @Size(max = 50, message = "Discount cannot exceed 50 characters")
    private String discount;

    @NotNull(message = "Tax rate is required.")
    private Long taxRateId;

    private String description;
}
