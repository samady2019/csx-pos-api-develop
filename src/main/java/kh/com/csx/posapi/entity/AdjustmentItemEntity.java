package kh.com.csx.posapi.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import kh.com.csx.posapi.dto.product.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "adjustment_items")
public class AdjustmentItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;
    private Long unitId;
    private Long warehouseId;
    private Double unitQuantity;
    private Double quantity;
    private Double unitCost;
    private Double baseUnitCost;
    private String type;
    private LocalDate expiry;

    // @ManyToOne
    // @JoinColumn(name = "product_id", referencedColumnName = "product_id", insertable = false, updatable = false)
    // private ProductEntity product;

    @Transient
    private ProductResponse product;

    @ManyToOne
    @JoinColumn(name = "adjustment_id")
    @JsonBackReference
    private AdjustmentEntity adjustment;
}

