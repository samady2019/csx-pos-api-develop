package kh.com.csx.posapi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static kh.com.csx.posapi.constant.Constant.DateTime.DATETIME_FORMAT;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "expenses")
public class ExpenseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    private LocalDateTime date;

    @Column(name = "reference_no")
    private String referenceNo;

    @Column(name = "biller_id")
    private Long billerId;

    @Column(name = "expense_category_id")
    private Long expenseCategoryId;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "paid")
    private Double paid;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "currencies")
    private String currencies;

    @Column(name = "attachment")
    private String attachment;

    @Column(name = "note")
    private String note;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "expense_category_id", referencedColumnName = "id", insertable = false, updatable = false)
    private ExpenseCategoryEntity expenseCategory;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "biller_id", referencedColumnName = "id", insertable = false, updatable = false)
    private BillerEntity biller;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", referencedColumnName = "id")
    private List<PaymentEntity> payments;


}
