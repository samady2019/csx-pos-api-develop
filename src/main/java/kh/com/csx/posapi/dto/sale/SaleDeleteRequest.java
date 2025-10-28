package kh.com.csx.posapi.dto.sale;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaleDeleteRequest {
    @NotNull(message = "Sale ID is required.")
    private Long id;
}
