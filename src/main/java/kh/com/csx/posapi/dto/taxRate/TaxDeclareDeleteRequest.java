package kh.com.csx.posapi.dto.taxRate;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaxDeclareDeleteRequest {
    @NotNull(message = "Tax declare ID is required.")
    private Object id;
}
