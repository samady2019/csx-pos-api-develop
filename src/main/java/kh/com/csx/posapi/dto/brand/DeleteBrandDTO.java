package kh.com.csx.posapi.dto.brand;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteBrandDTO {
    @NotNull(message = "Brand ID is required.")
    private Object brandId;
}
