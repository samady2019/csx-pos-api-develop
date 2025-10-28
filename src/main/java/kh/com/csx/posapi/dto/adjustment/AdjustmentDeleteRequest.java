package kh.com.csx.posapi.dto.adjustment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdjustmentDeleteRequest {
    @NotNull(message = "Adjustment ID is required.")
    private Object id;
}
