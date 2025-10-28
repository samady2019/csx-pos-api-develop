package kh.com.csx.posapi.dto.report.financialReport;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class ExpenseMonthlyRequest extends ExpenseRequest {
    private String code;
    private String name;
    private String month;
    private String year;
    private Integer all = 0;
}
