package kh.com.csx.posapi.dto.transfer;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import kh.com.csx.posapi.constant.Constant.DateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferItemRequest {
    @NotNull(message = "Product ID is required.")
    private Long productId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATE_FORMAT)
    private LocalDate expiry;

    @NotNull(message = "Unit ID is required.")
    private Long unitId;

    @NotNull(message = "Unit quantity is required.")
    @Positive(message = "Unit quantity must be positive")
    private Double unitQuantity;
}
