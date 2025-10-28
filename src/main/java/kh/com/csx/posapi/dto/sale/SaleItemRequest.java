package kh.com.csx.posapi.dto.sale;

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
public class SaleItemRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATE_FORMAT)
    private LocalDate expiry;

    @NotNull(message = "Unit ID is required.")
    private Long unitId;

    @NotNull(message = "Unit quantity is required.")
    @Positive(message = "Unit quantity must be positive")
    private Double unitQuantity;

    @NotNull(message = "Unit price is required.")
    @PositiveOrZero(message = "Unit price be zero or positive")
    private Double unitPrice;

    @Size(max = 50, message = "Discount cannot exceed 50 characters")
    private String discount;

    @NotNull(message = "Tax rate is required.")
    private Long taxRateId;

    private String description;
}
