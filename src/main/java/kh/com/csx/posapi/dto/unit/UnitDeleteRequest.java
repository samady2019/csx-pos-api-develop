package kh.com.csx.posapi.dto.unit;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnitDeleteRequest {
    @NotNull(message = "Unit ID is required.")
    private Object unitId;
}
