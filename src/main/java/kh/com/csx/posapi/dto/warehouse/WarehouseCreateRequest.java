package kh.com.csx.posapi.dto.warehouse;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseCreateRequest {
    @NotBlank(message = "Warehouse code is required")
    @Size(max = 255, message = "Warehouse code should not exceed {max} characters.")
    private String code;

    @NotBlank(message = "Warehouse name is required")
    @Size(max = 255, message = "Warehouse name should not exceed {max} characters.")
    private String name;

    @Size(max = 50, message = "Fax number should not exceed {max} characters.")
    private String fax;

    @Size(max = 50, message = "Phone should not exceed {max} characters.")
    private String phone;

    @Email(message = "Email is not valid")
    @Size(max = 50, message = "Email should not exceed {max} characters.")
    private String email;

    @Size(max = 255, message = "Address should not exceed {max} characters.")
    private String address;

    @Size(max = 255, message = "Map (location) should not exceed {max} characters.")
    private String map;

    private Integer overselling;
}
