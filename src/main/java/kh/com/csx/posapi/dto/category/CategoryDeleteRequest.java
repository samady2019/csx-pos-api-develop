package kh.com.csx.posapi.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDeleteRequest {
    @NotNull(message = "Category ID is required.")
    private Object categoryId;
}
