package kh.com.csx.posapi.dto.expenseCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseCategoryUpdateRequest {

    @NotNull(message = "ID is required.")
    private Long id;

    @NotBlank(message = "Code is required.")
    @Size(max = 255, message = "Code should not exceed {max} characters.")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Code can only contain letters, numbers, underscores or hyphens.")
    private String code;

    @NotBlank(message = "Name is required.")
    @Size(max = 255, message = "Name should not exceed {max} characters.")
    private String name;

    @Size(max = 255, message = "Description should not exceed {max} characters.")
    private String description;

    private Long parentId;
}

