package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.entity.WarehouseProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
public interface WarehouseProductRepository extends JpaRepository<WarehouseProductEntity, Long> {
    Optional<WarehouseProductEntity> findById(Long id);

    List<WarehouseProductEntity> findByProductId(Long productId);

    @Query(value = """
            SELECT * FROM warehouses_products
            WHERE
                ((:whIds IS NULL OR :whIds = '') OR FIND_IN_SET(warehouse_id, :whIds)) AND
                ((:whId IS NULL OR :whId = '') OR warehouse_id = :whId) AND
                product_id = :pId
            """,
    nativeQuery = true)
    List<WarehouseProductEntity> findWarehousesProduct(Long pId, String whIds, Long whId);
}
