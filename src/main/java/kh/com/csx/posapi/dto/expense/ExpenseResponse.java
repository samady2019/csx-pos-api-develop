package kh.com.csx.posapi.dto.expense;

import kh.com.csx.posapi.entity.ExpenseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseResponse {

    private ExpenseEntity expense;
}
