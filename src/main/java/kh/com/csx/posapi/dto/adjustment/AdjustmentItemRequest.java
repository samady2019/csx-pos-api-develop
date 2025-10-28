package kh.com.csx.posapi.dto.adjustment;

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
@NoArgsConstructor
@AllArgsConstructor
public class AdjustmentItemRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Type is required")
    @Size(max = 50, message = "Type should not exceed {max} characters.")
    private String type;

    @NotNull(message = "Unit ID is required")
    private Long unitId;

    @Positive(message = "Unit quantity must be positive")
    private Double unitQuantity;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATE_FORMAT)
    private LocalDate expiry;
}
