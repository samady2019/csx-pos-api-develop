package kh.com.csx.posapi.dto.report.financialReport;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class PaymentRequest extends FilterDTO {
    private Long id;
    private Long saleOrderId;
    private Long saleId;
    private Long purchaseOrderId;
    private Long purchaseId;
    private Long expenseId;
    private String referenceNo;
    private Long billerId;
    private Long warehouseId;
    private Long supplierId;
    private Long customerId;
    private Long paymentMethodId;
    private String paymentMethodName;
    private String type;
    private String transaction;
    private String status;
    private String paymentStatus;
    private Long createdBy;
    private Long updatedBy;
    private String startDate;
    private String endDate;

    private Integer tranPurchaseOrder;
    private Integer tranPurchase;
    private Integer tranPurchaseReturn;
    private Integer tranSaleOrder;
    private Integer tranSale;
    private Integer tranSaleReturn;
    private Integer tranExpense;
}
