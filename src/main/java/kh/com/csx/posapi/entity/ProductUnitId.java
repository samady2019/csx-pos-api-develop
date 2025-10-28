package kh.com.csx.posapi.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.util.Objects;

@Data
@Builder
@Embeddable
public class ProductUnitId implements Serializable {
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "unit_id")
    private Long unitId;

    public ProductUnitId() {}

    public ProductUnitId(Long productId, Long unitId) {
        this.productId = productId;
        this.unitId = unitId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductUnitId that = (ProductUnitId) o;
        return productId.equals(that.productId) && unitId.equals(that.unitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, unitId);
    }
}
