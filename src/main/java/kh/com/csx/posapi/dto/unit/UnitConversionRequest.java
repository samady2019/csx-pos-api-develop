package kh.com.csx.posapi.dto.unit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnitConversionRequest {
    @NotNull(message = "Unit ID is required")
    private Long unitId;

    @NotNull(message = "Value is required")
    private Double value;
}
