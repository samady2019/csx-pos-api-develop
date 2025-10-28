package kh.com.csx.posapi.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCreateRequest {
    private Long parentId;

    @NotBlank(message = "Code is required.")
    @Size(max = 255, message = "Code should not exceed {max} characters.")
    private String code;

    @NotBlank(message = "Name is required.")
    @Size(max = 255, message = "Name should not exceed {max} characters.")
    private String name;

    @Size(max = 255, message = "Description should not exceed {max} characters.")
    private String description;

    private Integer status;
}
