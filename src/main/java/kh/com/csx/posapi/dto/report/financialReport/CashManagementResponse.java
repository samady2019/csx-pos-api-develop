package kh.com.csx.posapi.dto.report.financialReport;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CashManagementResponse {
    private Long id;
    private String name;
    private String type;
    private Double beginning;
    private Double purchaseOrder;
    private Double purchase;
    private Double saleOrder;
    private Double sale;
    private Double expense;
    private Double balance;

    public CashManagementResponse(Tuple data) {
        this.id            = ((Number) data.get("id")).longValue();
        this.name          = (String) data.get("name");
        this.type          = (String) data.get("type");
        this.beginning     = ((Number) data.get("beginning")).doubleValue();
        this.purchaseOrder = ((Number) data.get("purchaseOrder")).doubleValue();
        this.purchase      = ((Number) data.get("purchase")).doubleValue();
        this.saleOrder     = ((Number) data.get("saleOrder")).doubleValue();
        this.sale          = ((Number) data.get("sale")).doubleValue();
        this.expense       = ((Number) data.get("expense")).doubleValue();
        this.balance       = ((Number) data.get("balance")).doubleValue();
    }
}
