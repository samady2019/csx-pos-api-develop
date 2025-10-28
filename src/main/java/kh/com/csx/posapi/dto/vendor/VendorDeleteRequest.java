package kh.com.csx.posapi.dto.vendor;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorDeleteRequest {
    @NotNull(message = "Vendor ID is required.")
    private Object id;
}
