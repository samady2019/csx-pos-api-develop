package kh.com.csx.posapi.repository;

import jakarta.persistence.Tuple;
import kh.com.csx.posapi.dto.product.ProductRetrieveDTO;
import kh.com.csx.posapi.dto.taxRate.TaxRateProductRetrieveRequest;
import kh.com.csx.posapi.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByProductId(Long productId);
    Optional<ProductEntity> findByProductCode(String productCode);
    boolean existsByProductCode(String productCode);
    boolean existsByBarCode(String barCode);
    boolean existsByBrandId(Long brandId);
    boolean existsByCategoryId(Long categoryId);

    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.createdBy = :userId OR p.updatedBy = :userId")
    long countProductsByUser(Long userId);

    @Override
    long count();

    @Query(value = "SELECT p FROM ProductEntity p LEFT JOIN WarehouseProductEntity wp ON wp.productId = p.productId " +
            "WHERE p.status = 1 AND " +
            "(:standard IS NULL OR :standard = false OR (:standard = true AND p.type = 'standard')) AND " +
            "(" +
            "   LOWER(p.productCode) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "   LOWER(p.barCode) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "   LOWER(p.productNameEn) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "   LOWER(p.productNameKh) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "   LOWER(CONCAT(p.productNameEn, ' (', p.productCode, ')')) LIKE LOWER(CONCAT('%', :term, '%'))" +
            ") AND " +
            "(:inStockOnly IS NULL OR :inStockOnly = false OR :warehouseId IS NULL OR (:inStockOnly = true AND wp.warehouseId = :warehouseId) OR (:inStockOnly = true AND :standard = false AND p.type != 'standard')) AND " +
            "(:inStockOnly IS NULL OR :inStockOnly = false OR (:inStockOnly = true AND wp.quantity > 0) OR (:inStockOnly = true AND :standard = false AND p.type != 'standard')) " +
            "GROUP BY p.productId " +
            "ORDER BY p.productCode ASC LIMIT 20 ")
    List<ProductEntity> findByTerm(String term, Boolean standard, Long warehouseId, Boolean inStockOnly);

    @Query(value = "SELECT p FROM ProductEntity p LEFT JOIN WarehouseProductEntity wp ON wp.productId = p.productId " +
            "WHERE p.status = 1 AND " +
            "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
            "(:brandId IS NULL OR p.brandId = :brandId) AND " +
            "(:standard IS NULL OR :standard = false OR (:standard = true AND p.type = 'standard')) AND " +
            "(:inStockOnly IS NULL OR :inStockOnly = false OR :warehouseId IS NULL OR (:inStockOnly = true AND wp.warehouseId = :warehouseId) OR (:inStockOnly = true AND :standard = false AND p.type != 'standard')) AND " +
            "(:inStockOnly IS NULL OR :inStockOnly = false OR (:inStockOnly = true AND wp.quantity > 0) OR (:inStockOnly = true AND :standard = false AND p.type != 'standard')) " +
            "GROUP BY p.productId ")
    Page<ProductEntity> findByBrandCategory(Long categoryId, Long brandId, Boolean standard, Long warehouseId, Boolean inStockOnly, Pageable pageable);

    @Query(value = """
        SELECT p FROM ProductEntity p
        LEFT JOIN CategoryEntity c ON c.categoryId = p.categoryId
        LEFT JOIN BrandEntity b ON b.brandId = p.brandId
        LEFT JOIN (
            SELECT
                _u.unitId AS unitId, _u.unitCode AS unitCode, _u.unitNameEn AS unitNameEn, _u.unitNameKh AS unitNameKh,
                _pu.id.productId AS productId, _pu.cost AS cost, _pu.price AS price
            FROM UnitEntity _u
            INNER JOIN ProductUnitEntity _pu ON _pu.id.unitId = _u.unitId AND _pu.defaultUnit = 1
        ) u ON u.productId = p.productId
        LEFT JOIN (
            SELECT wp.productId AS productId, COALESCE(SUM(quantity), 0) AS quantity
            FROM WarehouseProductEntity wp
            WHERE 1=1
                AND (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR wp.warehouseId = :#{#filter.warehouseId})
                AND (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR wp.productId = :#{#filter.productId})
            GROUP BY wp.productId
        ) s ON s.productId = p.productId
        WHERE 1=1
            AND (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR p.productId = :#{#filter.productId})
            AND (:#{#filter.productCode} IS NULL OR :#{#filter.productCode} = '' OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :#{#filter.productCode}, '%')))
            AND (:#{#filter.barCode} IS NULL OR :#{#filter.barCode} = '' OR LOWER(p.barCode) LIKE LOWER(CONCAT('%', :#{#filter.barCode}, '%')))
            AND (:#{#filter.productNameEn} IS NULL OR :#{#filter.productNameEn} = '' OR LOWER(p.productNameEn) LIKE LOWER(CONCAT('%', :#{#filter.productNameEn}, '%')))
            AND (:#{#filter.productNameKh} IS NULL OR :#{#filter.productNameKh} = '' OR LOWER(p.productNameKh) LIKE LOWER(CONCAT('%', :#{#filter.productNameKh}, '%')))
            AND (:#{#filter.categoryId} IS NULL OR :#{#filter.categoryId} = '' OR p.categoryId = :#{#filter.categoryId})
            AND (:#{#filter.brandId} IS NULL OR :#{#filter.brandId} = '' OR p.brandId = :#{#filter.brandId})
            AND (:#{#filter.type} IS NULL OR :#{#filter.type} = '' OR p.type = :#{#filter.type})
            AND (:#{#filter.status} IS NULL OR :#{#filter.status} = '' OR p.status = :#{#filter.status})
            AND (
                  (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '')
                  OR (
                       LOWER(p.productCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                       LOWER(p.barCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                       LOWER(p.productNameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                       LOWER(p.productNameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                       LOWER(p.type) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                       LOWER(c.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                       LOWER(b.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                       LOWER(u.unitNameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                       LOWER(CASE WHEN p.status = 1 THEN 'active' ELSE 'inactive' END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                     )
                )
    """)
    Page<ProductEntity> findAllByFilter(@Param("filter") ProductRetrieveDTO filter, Pageable pageable);

    @Query(value = """
        SELECT
            p.productId AS productId,
            p.image AS image,
            p.type AS type,
            p.productCode AS productCode,
            p.barCode AS barCode,
            p.productNameEn AS productNameEn,
            p.productNameKh AS productNameKh,
            c.name AS category,
            b.name AS brand,
            p.currency AS currency,
            u.unitCode AS unitCode,
            u.unitNameEn AS unitNameEn,
            u.unitNameKh AS unitNameKh,
            p.taxMethod AS taxMethod,
            p.taxRateDeclare AS taxRateDeclare,
            COALESCE(u.cost, 0) AS cost,
            COALESCE(u.price, 0) AS price,
            ROUND((COALESCE(u.cost, 0) * IFNULL(p.taxRateDeclare, COALESCE(s.defaultTaxRateDeclare, 100))) / 100, s.decimals) AS taxCost,
            ROUND((COALESCE(u.price, 0) * IFNULL(p.taxRateDeclare, COALESCE(s.defaultTaxRateDeclare, 100))) / 100, s.decimals) AS taxPrice
        FROM ProductEntity p
        LEFT JOIN BrandEntity b ON b.brandId = p.brandId
        LEFT JOIN CategoryEntity c ON c.categoryId = p.categoryId
        LEFT JOIN (
            SELECT
                _u.unitId AS unitId, _u.unitCode AS unitCode, _u.unitNameEn AS unitNameEn, _u.unitNameKh AS unitNameKh,
                _pu.id.productId AS productId,
                _pu.cost AS cost,
                _pu.price AS price
            FROM UnitEntity _u
            INNER JOIN ProductUnitEntity _pu ON _pu.id.unitId = _u.unitId AND _u.punitId IS NULL
        ) u ON u.productId = p.productId
        LEFT JOIN SettingEntity s ON 1=1
        WHERE
            p.status = 1 AND
            (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR p.productId = :#{#filter.productId}) AND
            (:#{#filter.brandId} IS NULL OR :#{#filter.brandId} = '' OR p.brandId = :#{#filter.brandId}) AND
            (:#{#filter.categoryId} IS NULL OR :#{#filter.categoryId} = '' OR p.categoryId = :#{#filter.categoryId}) AND
            (:#{#filter.type} IS NULL OR :#{#filter.type} = '' OR p.type = :#{#filter.type})
            AND (
                  (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '')
                  OR (
                       LOWER(p.productCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                       LOWER(p.barCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                       LOWER(p.productNameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                       LOWER(p.productNameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                       LOWER(p.type) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                       LOWER(c.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                       LOWER(b.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                       LOWER(u.unitNameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                     )
                )
    """)
    Page<Tuple> findTaxRateProducts(@Param("filter") TaxRateProductRetrieveRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            p.productId AS productId,
            p.image AS image,
            p.type AS type,
            p.productCode AS productCode,
            p.barCode AS barCode,
            p.productNameEn AS productNameEn,
            p.productNameKh AS productNameKh,
            c.name AS category,
            b.name AS brand,
            p.currency AS currency,
            u.unitCode AS unitCode,
            u.unitNameEn AS unitNameEn,
            u.unitNameKh AS unitNameKh,
            p.taxMethod AS taxMethod,
            p.taxRateDeclare AS taxRateDeclare,
            COALESCE(u.cost, 0) AS cost,
            COALESCE(u.price, 0) AS price,
            ROUND((COALESCE(u.cost, 0) * IFNULL(p.taxRateDeclare, COALESCE(s.defaultTaxRateDeclare, 100))) / 100, s.decimals) AS taxCost,
            ROUND((COALESCE(u.price, 0) * IFNULL(p.taxRateDeclare, COALESCE(s.defaultTaxRateDeclare, 100))) / 100, s.decimals) AS taxPrice
        FROM ProductEntity p
        LEFT JOIN BrandEntity b ON b.brandId = p.brandId
        LEFT JOIN CategoryEntity c ON c.categoryId = p.categoryId
        LEFT JOIN (
            SELECT
                _u.unitId AS unitId, _u.unitCode AS unitCode, _u.unitNameEn AS unitNameEn, _u.unitNameKh AS unitNameKh,
                _pu.id.productId AS productId,
                _pu.cost AS cost,
                _pu.price AS price
            FROM UnitEntity _u
            INNER JOIN ProductUnitEntity _pu ON _pu.id.unitId = _u.unitId AND _u.punitId IS NULL
        ) u ON u.productId = p.productId
        LEFT JOIN SettingEntity s ON 1=1
        WHERE p.status = 1 AND p.productId = :productId
    """)
    Tuple findTaxRateProduct(Long productId);

    @Query(value = """
        SELECT
            SUM(COALESCE(ref.count, 0)) AS count
        FROM (
            SELECT :productId AS productId, COUNT(*) AS count FROM adjustment_items aji     WHERE aji.product_id = :productId
            UNION ALL
            SELECT :productId AS productId, COUNT(*) AS count FROM promotion_items pmi      WHERE pmi.product_id = :productId
            UNION ALL
            SELECT :productId AS productId, COUNT(*) AS count FROM purchase_items pi        WHERE pi.product_id = :productId
            UNION ALL
            SELECT :productId AS productId, COUNT(*) AS count FROM purchase_order_items poi WHERE poi.product_id = :productId
            UNION ALL
            SELECT :productId AS productId, COUNT(*) AS count FROM sale_items si            WHERE si.product_id = :productId
            UNION ALL
            SELECT :productId AS productId, COUNT(*) AS count FROM stock_balance stk        WHERE stk.product_id = :productId
            UNION ALL
            SELECT :productId AS productId, COUNT(*) AS count FROM stock_count_items sci    WHERE sci.product_id = :productId
            UNION ALL
            SELECT :productId AS productId, COUNT(*) AS count FROM stock_movement sm        WHERE sm.product_id = :productId
            UNION ALL
            SELECT :productId AS productId, COUNT(*) AS count FROM 	suspended_items spi     WHERE spi.product_id = :productId
            UNION ALL
            SELECT :productId AS productId, COUNT(*) AS count FROM 	transfer_items tri      WHERE tri.product_id = :productId
            UNION ALL
            SELECT :productId AS productId, COUNT(*) AS count FROM 	warehouses_products	wp  WHERE wp.product_id = :productId
        ) ref
    """, nativeQuery = true)
    long countReferences(Long productId);
}