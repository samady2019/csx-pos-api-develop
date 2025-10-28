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
public class VendorUpdateRequest {
    @NotNull(message = "Vendor ID is required")
    private Long id;

    private Long userId;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name should not exceed {max} characters.")
    private String firstName;

    @Size(max = 50, message = "Last name should not exceed {max} characters.")
    private String lastName;

    @NotBlank(message = "Shop name EN is required")
    @Size(max = 255, message = "Shop name EN should not exceed {max} characters.")
    private String shopNameEn;

    @Size(max = 255, message = "Shop name KH should not exceed {max} characters.")
    private String shopNameKh;

    @Size(max = 255, message = "Address should not exceed {max} characters.")
    private String address;
}
