package kh.com.csx.posapi.dto.expense;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseDeleteRequest {
    @NotNull(message = "Expense ID is required.")
    private Object id;
}
