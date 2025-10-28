package kh.com.csx.posapi.dto.report.financialReport;

import jakarta.persistence.Tuple;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseMonthlyResponse {
    private Long id;
    private String code;
    private String name;
    private String year;
    private Double total;
    private List<ExpenseMonthly> expenseMonthly;

    @Data
    @NoArgsConstructor
    public static class ExpenseMonthly {
        private String month;
        private Double amount;

        public ExpenseMonthly(String month, Double amount) {
            this.month  = month;
            this.amount = amount;
        }
    }

    public ExpenseMonthlyResponse(Tuple category, List<Tuple> expenseMonthly) {
        this.id             = ((Number) category.get("id")).longValue();
        this.code           = (String) category.get("code");
        this.name           = (String) category.get("name");
        this.year           = (String) category.get("year");
        this.total          = ((Number) category.get("total")).doubleValue();
        this.expenseMonthly = new ArrayList<>();
        for (Tuple m : expenseMonthly) {
            ExpenseMonthly expense = new ExpenseMonthly(
                (String) m.get("month"),
                ((Number) m.get("amount")).doubleValue()
            );
            this.expenseMonthly.add(expense);
        }
    }
}
