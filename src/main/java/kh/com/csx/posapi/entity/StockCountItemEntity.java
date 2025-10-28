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
@Table(name = "stock_count_items")
public class StockCountItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_count_id")
    private Long stockCountId;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "expiry")
    private LocalDate expiry;

    @Column(name = "expected")
    private Double expected;

    @Column(name = "counted")
    private Double counted;

    @Column(name = "cost")
    private Double cost;

    @Column(name = "status")
    private Integer status;

    // @ManyToOne
    // @JoinColumn(name = "product_id", referencedColumnName = "product_id", insertable = false, updatable = false)
    // private ProductEntity product;

    @Transient
    private ProductResponse product;
}
