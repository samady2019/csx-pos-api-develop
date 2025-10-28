package kh.com.csx.posapi.dto.taxRate;

import kh.com.csx.posapi.constant.Constant;
import com.fasterxml.jackson.annotation.JsonFormat;
import kh.com.csx.posapi.entity.BillerEntity;
import kh.com.csx.posapi.entity.WarehouseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaxDeclareTransaction {
    private Long id;

    private Long taxDeclarationId;

    private Long transactionId;

    private String transaction;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATETIME_FORMAT)
    private LocalDateTime date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATE_FORMAT)
    private LocalDate dueDate;

    private String referenceNo;

    private String taxReferenceNo;

    private Long billerId;

    private Long warehouseId;

    private Long companyId;

    private String companyEn;

    private String companyKh;

    private String nameEn;

    private String nameKh;

    private String phone;

    private String email;

    private String vatNo;

    private Double quantity;

    private Double total;

    private Double shipping;

    private Double productDiscount;

    private String orderDiscountId;

    private Double orderDiscount;

    private Double totalDiscount;

    private Double productTax;

    private Long   orderTaxId;
    private String orderTaxName;
    private Double orderTaxValue;

    private Double orderTax;

    private Double totalTax;

    private Double grandTotal;

    private Integer totalItems;

    private Double exchangeRate;

    private String note;

    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByName;
    private String updatedByName;

    private BillerEntity biller;
    private WarehouseEntity warehouse;
    private Object company;

    List<TaxDeclareTransactionItem> transactionItems;
}
