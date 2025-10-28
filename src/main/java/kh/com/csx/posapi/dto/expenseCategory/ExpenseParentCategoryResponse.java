package kh.com.csx.posapi.dto.expenseCategory;

import kh.com.csx.posapi.entity.ExpenseParentCategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseParentCategoryResponse {

    private ExpenseParentCategoryEntity expenseParentCategoryEntity;

}
