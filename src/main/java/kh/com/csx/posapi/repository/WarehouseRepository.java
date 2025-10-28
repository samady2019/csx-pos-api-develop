package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.warehouse.WarehouseRetrieveRequest;
import kh.com.csx.posapi.entity.WarehouseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<WarehouseEntity, Long> {
    Optional<WarehouseEntity> findById(Long id);

    Optional<WarehouseEntity> findFirstByCode(String code);

    Optional<WarehouseEntity> findFirstByName(String name);

    Optional<WarehouseEntity> findFirstByCodeAndIdNot(String code, Long id);

    Optional<WarehouseEntity> findFirstByNameAndIdNot(String name, Long id);

    boolean existsBy();

    boolean existsByCode(String code);

    boolean existsByName(String name);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query(value = "SELECT w.* FROM warehouses w " +
            "LEFT JOIN v_users u ON u.user_id = :userId " +
            "WHERE (u.warehouses IS NOT NULL AND TRIM(u.warehouses) != '' AND FIND_IN_SET(w.id, u.warehouses)) OR (u.warehouses IS NULL OR TRIM(u.warehouses) = '')",
            nativeQuery = true)
    List<WarehouseEntity> findAllWarehouses(Long userId);

    @Query(value = "SELECT w FROM WarehouseEntity w WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR w.id = :#{#filter.id}) " +
            "AND (:#{#filter.whIds} IS NULL OR w.id IN :#{#filter.whIds}) " +
            "AND (:#{#filter.code} IS NULL OR :#{#filter.code} = '' OR w.code = :#{#filter.code}) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR w.name = :#{#filter.name}) " +
            "AND (:#{#filter.fax} IS NULL OR :#{#filter.fax} = '' OR w.fax LIKE CONCAT('%', :#{#filter.fax}, '%')) " +
            "AND (:#{#filter.phone} IS NULL OR :#{#filter.phone} = '' OR w.phone LIKE CONCAT('%', :#{#filter.phone}, '%')) " +
            "AND (:#{#filter.email} IS NULL OR :#{#filter.email} = '' OR w.email LIKE CONCAT('%', :#{#filter.email}, '%')) " +
            "AND (:#{#filter.overselling} IS NULL OR :#{#filter.overselling} = '' OR w.overselling = :#{#filter.overselling}) ")
    List<WarehouseEntity> findListByFilter(@Param("filter") WarehouseRetrieveRequest filter);

    @Query(value = "SELECT w FROM WarehouseEntity w WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR w.id = :#{#filter.id}) " +
            "AND (:#{#filter.whIds} IS NULL OR w.id IN :#{#filter.whIds}) " +
            "AND (:#{#filter.code} IS NULL OR :#{#filter.code} = '' OR w.code = :#{#filter.code}) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR w.name = :#{#filter.name}) " +
            "AND (:#{#filter.fax} IS NULL OR :#{#filter.fax} = '' OR w.fax LIKE CONCAT('%', :#{#filter.fax}, '%')) " +
            "AND (:#{#filter.phone} IS NULL OR :#{#filter.phone} = '' OR w.phone LIKE CONCAT('%', :#{#filter.phone}, '%')) " +
            "AND (:#{#filter.email} IS NULL OR :#{#filter.email} = '' OR w.email LIKE CONCAT('%', :#{#filter.email}, '%')) " +
            "AND (:#{#filter.overselling} IS NULL OR :#{#filter.overselling} = '' OR w.overselling = :#{#filter.overselling}) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(w.code) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(w.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(REPLACE(w.fax, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(TRIM(:#{#filter.term}), ' ', ''), '%')) OR " +
            "           LOWER(REPLACE(w.phone, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(TRIM(:#{#filter.term}), ' ', ''), '%')) OR " +
            "           LOWER(w.email) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(w.address) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    )")
    Page<WarehouseEntity> findAllByFilter(@Param("filter") WarehouseRetrieveRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            SUM(COALESCE(ref.count, 0)) AS count
        FROM (
            SELECT :warehouseId AS warehouseId, COUNT(*) AS count FROM purchases_order po     WHERE po.warehouse_id = :warehouseId
            UNION ALL
            SELECT :warehouseId AS warehouseId, COUNT(*) AS count FROM purchases p            WHERE p.warehouse_id = :warehouseId
            UNION ALL
            SELECT :warehouseId AS warehouseId, COUNT(*) AS count FROM sales s                WHERE s.warehouse_id = :warehouseId
            UNION ALL
            SELECT :warehouseId AS warehouseId, COUNT(*) AS count FROM suspended_bills sp     WHERE sp.warehouse_id = :warehouseId
            UNION ALL
            SELECT :warehouseId AS warehouseId, COUNT(*) AS count FROM adjustments aj         WHERE aj.warehouse_id = :warehouseId
            UNION ALL
            SELECT :warehouseId AS warehouseId, COUNT(*) AS count FROM transfers tr           WHERE tr.from_warehouse = :warehouseId OR tr.to_warehouse = :warehouseId
            UNION ALL
            SELECT :warehouseId AS warehouseId, COUNT(*) AS count FROM stock_counts sc        WHERE sc.warehouse_id = :warehouseId
            UNION ALL
            SELECT :warehouseId AS warehouseId, COUNT(*) AS count FROM stock_movement sm      WHERE sm.warehouse_id = :warehouseId
            UNION ALL
            SELECT :warehouseId AS warehouseId, COUNT(*) AS count FROM warehouses_products wp WHERE wp.warehouse_id = :warehouseId
            UNION ALL
            SELECT :warehouseId AS warehouseId, COUNT(*) AS count FROM v_users u              WHERE FIND_IN_SET(:warehouseId, u.warehouses)
        ) ref
    """, nativeQuery = true)
    long countReferences(Long warehouseId);
}
