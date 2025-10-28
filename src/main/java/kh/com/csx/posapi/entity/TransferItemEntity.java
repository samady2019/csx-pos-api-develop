package kh.com.csx.posapi.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import kh.com.csx.posapi.dto.product.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transfer_items")
public class TransferItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_id")
    private Long transferId;

    private Long fromWarehouse;
    private Long toWarehouse;

    @Column(name = "product_id")
    private Long productId;

    private LocalDate expiry;
    private Long unitId;
    private Double unitQuantity;
    private Double quantity;
    private Double unitCost;
    private Double baseUnitCost;

    // @ManyToOne
    // @JoinColumn(name = "product_id", referencedColumnName = "product_id", insertable = false, updatable = false)
    // private ProductEntity product;

    @Transient
    private ProductResponse product;
}
