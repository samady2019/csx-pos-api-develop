package kh.com.csx.posapi.dto.expenseCategory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseCategoryParentRequest {

    @NotNull(message = "Parent ID is required.")
    private Long parentId;

}

