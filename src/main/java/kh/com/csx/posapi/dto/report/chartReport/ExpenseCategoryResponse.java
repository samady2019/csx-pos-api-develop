package kh.com.csx.posapi.dto.report.chartReport;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class ExpenseCategoryResponse {
    private String month;
    private String year;
    private List<Expense> expenses;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Expense {
        private Long id;
        private String code;
        private String name;
        private Double total;

        public Expense(Tuple data) {
            this.id    = ((Number) data.get("id")).longValue();
            this.code  = (String) data.get("code");
            this.name  = (String) data.get("name");
            this.total = ((Number) data.get("total")).doubleValue();
        }
    }

    public ExpenseCategoryResponse(String month, String year, List<Tuple> data) {
        this.month = month;
        this.year  = year;
        this.expenses = data.stream().map(Expense::new).toList();
    }
}
