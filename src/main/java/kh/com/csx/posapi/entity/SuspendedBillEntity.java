package kh.com.csx.posapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import static kh.com.csx.posapi.constant.Constant.DateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "suspended_bills")
public class SuspendedBillEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "reference_no", length = 50)
    private String referenceNo;

    @Column(name = "biller_id")
    private Long billerId;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "total")
    private Double total;

    @Column(name = "shipping")
    private Double shipping;

    @Column(name = "product_discount")
    private Double productDiscount;

    @Column(name = "order_discount_id", length = 50)
    private String orderDiscountId;

    @Column(name = "order_discount")
    private Double orderDiscount;

    @Column(name = "total_discount")
    private Double totalDiscount;

    @Column(name = "product_tax")
    private Double productTax;

    @Column(name = "order_tax_id")
    private Long orderTaxId;

    @Column(name = "order_tax")
    private Double orderTax;

    @Column(name = "total_tax")
    private Double totalTax;

    @Column(name = "grand_total")
    private Double grandTotal;

    @Column(name = "total_items")
    private Integer totalItems;

    @Column(name = "status")
    private Integer status;

    @Column(name = "attachment", length = 255)
    private String attachment;

    @Column(name = "suspend_note")
    private String suspendNote;

    @Column(name = "staff_note")
    private String staffNote;

    @Column(name = "note")
    private String note;

    @Column(name = "salesman_by")
    private Long salesmanBy;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "biller_id", referencedColumnName = "id", insertable = false, updatable = false)
    private BillerEntity biller;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", referencedColumnName = "id", insertable = false, updatable = false)
    private WarehouseEntity warehouse;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id", insertable = false, updatable = false)
    private CustomerEntity customer;

    @ManyToOne
    @JoinColumn(name = "salesman_by", referencedColumnName = "user_id", insertable = false, updatable = false)
    private UserEntity salesman;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "suspend_id", referencedColumnName = "id")
    private List<SuspendedItemEntity> items;
}
