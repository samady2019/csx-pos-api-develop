package kh.com.csx.posapi.dto.brand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateBrandDTO {
    @NotBlank(message = "Code is required")
    @Size(max = 255, message = "Code should not exceed {max} characters.")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name should not exceed {max} characters.")
    private String name;

    private Integer status;
}
