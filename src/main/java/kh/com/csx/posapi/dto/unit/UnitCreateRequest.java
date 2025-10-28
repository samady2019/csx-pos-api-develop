package kh.com.csx.posapi.dto.unit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnitCreateRequest {
    private Long punitId;

    @NotBlank(message = "Unit code is required")
    private String unitCode;

    @NotBlank(message = "Unit name is required")
    private String unitNameEn;

    private String unitNameKh;

    @NotNull(message = "Value is required")
    @DecimalMin(value = "1.0", message = "Value must be a number and Minimum 1")
    private double value;

    private String description;
}
