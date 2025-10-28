package kh.com.csx.posapi.dto.promotion;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionItemRequest {
    @NotNull(message = "Product ID is required.")
    private Long productId;

    @NotNull(message = "Discount (%) is required.")
    @Positive(message = "Discount (%) must be positive.")
    private Double discount;
}
