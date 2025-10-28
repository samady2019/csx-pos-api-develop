package kh.com.csx.posapi.dto.report.financialReport;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfitAndOrLossResponse {
    private Double beginning;
    private Double totalNetSale;
    private Double totalCostOfGood;
    private Double totalPurchase;
    private Double totalExpense;
    private Double totalProfit;
    private Double ending;

    private List<Object> revenues;
    private Purchase purchase;
    private List<Expense> expenses;

    @Data
    @NoArgsConstructor
    private static class Sale {
        private Double discount;
        private Double tax;
        private Double shipping;
        private Double refund;
        private Double total;

        public Sale(Double discount, Double tax, Double shipping, Double refund, Double total) {
            this.discount = discount;
            this.tax      = tax;
            this.shipping = shipping;
            this.refund   = refund;
            this.total    = total;
        }
    }

    @Data
    @NoArgsConstructor
    private static class Purchase {
        private Double discount;
        private Double tax;
        private Double shipping;
        private Double refund;
        private Double total;

        public Purchase(Double discount, Double tax, Double shipping, Double refund, Double total) {
            this.discount = discount;
            this.tax      = tax;
            this.shipping = shipping;
            this.refund   = refund;
            this.total    = total;
        }
    }

    @Data
    @NoArgsConstructor
    private static class Expense {
        private String category;
        private Double total;

        public Expense(String category, Double total) {
            this.category = category;
            this.total    = total;
        }
    }

    public ProfitAndOrLossResponse(Tuple data, List<Tuple> dataRevenues, Tuple dataPurchase, List<Tuple> dataExpenses) {
        this.beginning       = ((Number) data.get("beginning")).doubleValue();
        this.totalNetSale    = ((Number) data.get("totalNetSale")).doubleValue();
        this.totalCostOfGood = ((Number) data.get("totalCostOfGood")).doubleValue();
        this.totalPurchase   = ((Number) data.get("totalPurchase")).doubleValue();
        this.totalExpense    = ((Number) data.get("totalExpense")).doubleValue();
        this.totalProfit     = ((Number) data.get("totalProfit")).doubleValue();
        this.ending          = ((Number) data.get("ending")).doubleValue();
        this.revenues        = new ArrayList<>();
        this.purchase        = new Purchase();
        this.expenses        = new ArrayList<>();

        for (Tuple revenue : dataRevenues) {
            Sale sale = new Sale(
                    ((Number) revenue.get("discount")).doubleValue(),
                    ((Number) revenue.get("tax")).doubleValue(),
                    ((Number) revenue.get("shipping")).doubleValue(),
                    ((Number) revenue.get("refund")).doubleValue(),
                    ((Number) revenue.get("total")).doubleValue()
            );
            revenues.add(sale);
        }
        purchase = new Purchase(
            ((Number) dataPurchase.get("discount")).doubleValue(),
            ((Number) dataPurchase.get("tax")).doubleValue(),
            ((Number) dataPurchase.get("shipping")).doubleValue(),
            ((Number) dataPurchase.get("refund")).doubleValue(),
            ((Number) dataPurchase.get("total")).doubleValue()
        );
        for (Tuple xp : dataExpenses) {
            Expense expense = new Expense(
                ((String) xp.get("category")),
                ((Number) xp.get("total")).doubleValue()
            );
            expenses.add(expense);
        }
    }
}
