package kh.com.csx.posapi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import kh.com.csx.posapi.constant.Constant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "biller_id")
    private Long billerId;

    @Column(name = "sale_order_id")
    private Long saleOrderId;

    @Column(name = "sale_id")
    private Long saleId;

    @Column(name = "purchase_order_id")
    private Long purchaseOrderId;

    @Column(name = "purchase_id")
    private Long purchaseId;

    @Column(name = "expense_id")
    private Long expenseId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATETIME_FORMAT)
    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "reference_no", length = 50)
    private String referenceNo;

    @Column(name = "payment_method_id")
    private Long paymentMethodId;

    @Column(name = "account_number", length = 255)
    private String accountNumber;

    @Column(name = "account_name", length = 255)
    private String accountName;

    @Column(name = "bank_name", length = 255)
    private String bankName;

    @Column(name = "cheque_no", length = 255)
    private String chequeNo;

    @Column(name = "cheque_date", length = 255)
    private String chequeDate;

    @Column(name = "cheque_number", length = 255)
    private String chequeNumber;

    @Column(name = "cc_no", length = 255)
    private String ccNo;

    @Column(name = "cc_cvv2", length = 255)
    private String ccCvv2;

    @Column(name = "cc_holder", length = 255)
    private String ccHolder;

    @Column(name = "cc_month", length = 255)
    private String ccMonth;

    @Column(name = "cc_year", length = 255)
    private String ccYear;

    @Column(name = "cc_type", length = 255)
    private String ccType;

    @Column(name = "currencies")
    private String currencies;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "note")
    private String note;

    @Column(name = "attachment", length = 255)
    private String attachment;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATETIME_FORMAT)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATETIME_FORMAT)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "payment_method_id", referencedColumnName = "id", insertable=false, updatable=false)
    private PaymentMethodEntity paymentMethod;

    @ManyToOne
    @JoinColumn(name = "biller_id", referencedColumnName = "id", insertable=false, updatable=false)
    private BillerEntity biller;
}
