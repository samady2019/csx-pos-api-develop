package kh.com.csx.posapi.dto.category;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryUpdateRequest {
    @NotNull(message = "Category ID is required.")
    private Long categoryId;

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
