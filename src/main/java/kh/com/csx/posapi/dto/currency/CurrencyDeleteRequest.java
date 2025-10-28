package kh.com.csx.posapi.dto.currency;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyDeleteRequest {
    @NotNull(message = "Currency ID is required.")
    private Object id;
}
