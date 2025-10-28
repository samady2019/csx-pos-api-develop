package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.paymentMethod.PaymentMethodRetrieveRequest;
import kh.com.csx.posapi.entity.PaymentMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethodEntity, Long> {
    Optional<PaymentMethodEntity> findById(Long id);

    @Query(value = "SELECT pm FROM PaymentMethodEntity pm WHERE pm.status = 1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR pm.id = :#{#filter.id}) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR pm.name = :#{#filter.name}) " +
            "AND (:#{#filter.type} IS NULL OR :#{#filter.type} = '' OR pm.type = :#{#filter.type}) ")
    List<PaymentMethodEntity> findListByFilter(@Param("filter") PaymentMethodRetrieveRequest filter);

    @Query(value = "SELECT pm FROM PaymentMethodEntity pm WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR pm.id = :#{#filter.id}) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR pm.name = :#{#filter.name}) " +
            "AND (:#{#filter.type} IS NULL OR :#{#filter.type} = '' OR pm.type = :#{#filter.type}) " +
            "AND (:#{#filter.status} IS NULL OR pm.status = :#{#filter.status}) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(pm.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(pm.type) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(pm.description) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CASE WHEN pm.status = 1 THEN 'active' ELSE 'inactive' END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    )")
    Page<PaymentMethodEntity> findAllByFilter(@Param("filter") PaymentMethodRetrieveRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            SUM(COALESCE(ref.count, 0)) AS count
        FROM (
            SELECT :id AS id, COUNT(*) AS count FROM payments p WHERE p.payment_method_id = :id
        ) ref
    """, nativeQuery = true)
    long countReferences(Long id);
}
