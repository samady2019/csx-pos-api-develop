package kh.com.csx.posapi.dto.warehouse;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseDeleteRequest {
    @NotNull(message = "Warehouse ID is required.")
    private Object id;
}
