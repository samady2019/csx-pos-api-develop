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
public class ExpenseCategoryDeleteRequest {
    @NotNull(message = "Expense category ID is required.")
    private Long id;
}

