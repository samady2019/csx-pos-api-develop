package kh.com.csx.posapi.dto.stockCount;

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
public class StockCountItemRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATE_FORMAT)
    private LocalDate expiry;

    @NotNull(message = "counted quantity is required.")
    private Double counted;
}
