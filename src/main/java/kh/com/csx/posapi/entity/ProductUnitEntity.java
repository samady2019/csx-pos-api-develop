package kh.com.csx.posapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_unit")
public class ProductUnitEntity {
    @EmbeddedId
    private ProductUnitId id;

    @Column(name = "cost", nullable = false)
    private double cost;

    @Column(name = "price", nullable = false)
    private double price;

    @Column(name = "default_unit")
    private int defaultUnit;

    @Column(name = "default_sale")
    private int defaultSale;

    @Column(name = "default_purchase")
    private int defaultPurchase;

    @ManyToOne
    @JoinColumn(name = "unit_id", referencedColumnName = "unit_id", insertable = false, updatable = false)
    private UnitEntity unit;
}
