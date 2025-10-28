package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.entity.ProductUnitEntity;
import kh.com.csx.posapi.entity.ProductUnitId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductUnitRepository extends JpaRepository<ProductUnitEntity, ProductUnitId> {
    @Query(value = "SELECT p FROM ProductUnitEntity p WHERE p.id.productId = :productId AND p.id.unitId = :unitId")
    ProductUnitEntity findProductUnit(Long productId, Long unitId);

    @Query(value = "SELECT p FROM ProductUnitEntity p WHERE p.id.productId = :productId")
    List<ProductUnitEntity> findProductUnits(Long productId);

    @Modifying
    @Query("DELETE FROM ProductUnitEntity p WHERE p.id.productId = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}
