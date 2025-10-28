package kh.com.csx.posapi.dto.supplier;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierDeleteRequest {
    @NotNull(message = "Supplier ID is required.")
    private Object id;
}
