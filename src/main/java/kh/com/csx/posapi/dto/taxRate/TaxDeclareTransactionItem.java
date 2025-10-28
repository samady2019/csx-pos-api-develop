package kh.com.csx.posapi.dto.taxRate;

import jakarta.persistence.Transient;
import kh.com.csx.posapi.dto.product.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaxDeclareTransactionItem {
    private Long      id;
    private Long      transactionId;
    private String    transaction;
    private Long      itemId;
    private Double    taxRateDeclare;
    private Long      warehouseId;

    private Long      productId;
    private String    productCode;
    private String    productBarCode;
    private String    productNameEn;
    private String    productNameKh;

    private LocalDate expiry;

    private Long      unitId;
    private String    unitCode;
    private String    unitNameEn;
    private String    unitNameKh;

    private Double    unitQuantity;
    private Double    quantity;
    private Double    unitPrice;
    private Double    baseUnitPrice;
    private String    discount;
    private Double    itemDiscount;
    private Long      taxRateId;
    private String    taxRateName;
    private Double    taxRateValue;
    private Double    itemTax;
    private Double    subtotal;
    private String    description;

    // @Transient
    // private ProductResponse product;
}
