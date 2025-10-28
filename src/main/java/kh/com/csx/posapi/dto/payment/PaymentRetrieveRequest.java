package kh.com.csx.posapi.dto.payment;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRetrieveRequest extends FilterDTO {
    private Long id;
    private String referenceNo;
    private Long billerId;
    private Long saleOrderId;
    private Long saleId;
    private Long purchaseOrderId;
    private Long purchaseId;
    private Long expenseId;
    private Long paymentMethodId;
    private String type;
    private String transaction;
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
