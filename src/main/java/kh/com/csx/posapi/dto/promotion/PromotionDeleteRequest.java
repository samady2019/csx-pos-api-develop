package kh.com.csx.posapi.dto.promotion;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionDeleteRequest {
    @NotNull(message = "Promotion ID is required.")
    private Object id;
}
