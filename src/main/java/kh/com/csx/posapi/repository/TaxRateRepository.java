package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.taxRate.TaxRateRetrieveRequest;
import kh.com.csx.posapi.entity.TaxRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaxRateRepository extends JpaRepository<TaxRateEntity, Long> {
    Optional<TaxRateEntity> findById(Long id);

    Optional<TaxRateEntity> findFirstByCode(String code);

    Optional<TaxRateEntity> findFirstByCodeAndIdNot(String code, Long id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Optional<TaxRateEntity> findFirstByName(String name);

    Optional<TaxRateEntity> findFirstByNameAndIdNot(String name, Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query(value = "SELECT t FROM TaxRateEntity t WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR t.id = :#{#filter.id}) " +
            "AND (:#{#filter.code} IS NULL OR :#{#filter.code} = '' OR t.code = :#{#filter.code}) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR t.name = :#{#filter.name}) ")
    List<TaxRateEntity> findListByFilter(@Param("filter") TaxRateRetrieveRequest filter);

    @Query(value = "SELECT t FROM TaxRateEntity t WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR t.id = :#{#filter.id}) " +
            "AND (:#{#filter.code} IS NULL OR :#{#filter.code} = '' OR t.code = :#{#filter.code}) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR t.name = :#{#filter.name}) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(t.code) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(t.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    )")
    Page<TaxRateEntity> findAllByFilter(@Param("filter") TaxRateRetrieveRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            SUM(COALESCE(ref.count, 0)) AS count
        FROM (
            SELECT :id AS id, COUNT(*) AS count FROM purchases p              WHERE p.order_tax_id = :id
            UNION ALL
            SELECT :id AS id, COUNT(*) AS count FROM purchase_items pi        WHERE pi.tax_rate_id = :id
            UNION ALL
            SELECT :id AS id, COUNT(*) AS count FROM purchases_order po       WHERE po.order_tax_id = :id
            UNION ALL
            SELECT :id AS id, COUNT(*) AS count FROM purchase_order_items poi WHERE poi.tax_rate_id = :id
            UNION ALL
            SELECT :id AS id, COUNT(*) AS count FROM sales s                  WHERE s.order_tax_id = :id
            UNION ALL
            SELECT :id AS id, COUNT(*) AS count FROM sale_items si            WHERE si.tax_rate_id = :id
            UNION ALL
            SELECT :id AS id, COUNT(*) AS count FROM suspended_bills sp       WHERE sp.order_tax_id = :id
            UNION ALL
            SELECT :id AS id, COUNT(*) AS count FROM suspended_items spi      WHERE spi.tax_rate_id = :id
        ) ref
    """, nativeQuery = true)
    long countReferences(Long id);
}
