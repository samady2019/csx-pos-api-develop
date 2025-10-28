package kh.com.csx.posapi.entity;

import jakarta.persistence.*;
import kh.com.csx.posapi.dto.product.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sale_items")
public class SaleItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sale_id")
    private Long saleId;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "expiry")
    private LocalDate expiry;

    @Column(name = "unit_id")
    private Long unitId;

    @Column(name = "unit_quantity")
    private Double unitQuantity;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "base_unit_cost")
    private Double baseUnitCost;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "base_unit_price")
    private Double baseUnitPrice;

    @Column(name = "net_unit_price")
    private Double netUnitPrice;

    @Column(name = "discount", length = 50)
    private String discount;

    @Column(name = "item_discount")
    private Double itemDiscount;

    @Column(name = "tax_rate_id")
    private Long taxRateId;

    @Column(name = "item_tax")
    private Double itemTax;

    @Column(name = "subtotal")
    private Double subtotal;

    @Column(name = "description")
    private String description;

    // @ManyToOne
    // @JoinColumn(name = "product_id", referencedColumnName = "product_id", insertable = false, updatable = false)
    // private ProductEntity product;

    @Transient
    private ProductResponse product;
}
