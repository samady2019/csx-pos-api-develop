package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.biller.BillerRetrieveRequest;
import kh.com.csx.posapi.entity.BillerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillerRepository extends JpaRepository<BillerEntity, Long> {
    Optional<BillerEntity> findById(Long id);

    @Query("SELECT b.id FROM BillerEntity b")
    List<Long> findAllBillerIds();

    Optional<BillerEntity> findFirstByCode(String code);

    Optional<BillerEntity> findFirstByCodeAndIdNot(String code, Long id);

    boolean existsBy();

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query(value = "SELECT b FROM BillerEntity b WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR b.id = :#{#filter.id}) " +
            "AND (:#{#filter.ids} IS NULL OR b.id IN :#{#filter.ids}) " +
            "AND (:#{#filter.code} IS NULL OR :#{#filter.code} = '' OR b.code = :#{#filter.code}) " +
            "AND (:#{#filter.companyEn} IS NULL OR :#{#filter.companyEn} = '' OR b.companyEn LIKE CONCAT('%', :#{#filter.companyEn}, '%')) " +
            "AND (:#{#filter.companyKh} IS NULL OR :#{#filter.companyKh} = '' OR b.companyKh LIKE CONCAT('%', :#{#filter.companyKh}, '%')) " +
            "AND (:#{#filter.nameEn} IS NULL OR :#{#filter.nameEn} = '' OR b.nameEn LIKE CONCAT('%', :#{#filter.nameEn}, '%')) " +
            "AND (:#{#filter.nameKh} IS NULL OR :#{#filter.nameKh} = '' OR b.nameKh LIKE CONCAT('%', :#{#filter.nameKh}, '%')) " +
            "AND (:#{#filter.vatNo} IS NULL OR :#{#filter.vatNo} = '' OR b.vatNo = :#{#filter.vatNo}) " +
            "AND (:#{#filter.contactPerson} IS NULL OR :#{#filter.contactPerson} = '' OR b.contactPerson LIKE CONCAT('%', :#{#filter.contactPerson}, '%')) " +
            "AND (:#{#filter.phone} IS NULL OR :#{#filter.phone} = '' OR b.phone LIKE CONCAT('%', :#{#filter.phone}, '%')) " +
            "AND (:#{#filter.email} IS NULL OR :#{#filter.email} = '' OR b.email LIKE CONCAT('%', :#{#filter.email}, '%')) ")
    List<BillerEntity> findListByFilter(@Param("filter") BillerRetrieveRequest filter);

    @Query(value = "SELECT b FROM BillerEntity b WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR b.id = :#{#filter.id}) " +
            "AND (:#{#filter.ids} IS NULL OR b.id IN :#{#filter.ids}) " +
            "AND (:#{#filter.code} IS NULL OR :#{#filter.code} = '' OR b.code = :#{#filter.code}) " +
            "AND (:#{#filter.companyEn} IS NULL OR :#{#filter.companyEn} = '' OR b.companyEn LIKE CONCAT('%', :#{#filter.companyEn}, '%')) " +
            "AND (:#{#filter.companyKh} IS NULL OR :#{#filter.companyKh} = '' OR b.companyKh LIKE CONCAT('%', :#{#filter.companyKh}, '%')) " +
            "AND (:#{#filter.nameEn} IS NULL OR :#{#filter.nameEn} = '' OR b.nameEn LIKE CONCAT('%', :#{#filter.nameEn}, '%')) " +
            "AND (:#{#filter.nameKh} IS NULL OR :#{#filter.nameKh} = '' OR b.nameKh LIKE CONCAT('%', :#{#filter.nameKh}, '%')) " +
            "AND (:#{#filter.vatNo} IS NULL OR :#{#filter.vatNo} = '' OR b.vatNo = :#{#filter.vatNo}) " +
            "AND (:#{#filter.contactPerson} IS NULL OR :#{#filter.contactPerson} = '' OR b.contactPerson LIKE CONCAT('%', :#{#filter.contactPerson}, '%')) " +
            "AND (:#{#filter.phone} IS NULL OR :#{#filter.phone} = '' OR b.phone LIKE CONCAT('%', :#{#filter.phone}, '%')) " +
            "AND (:#{#filter.email} IS NULL OR :#{#filter.email} = '' OR b.email LIKE CONCAT('%', :#{#filter.email}, '%')) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(b.code) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(b.companyEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(b.companyKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(b.nameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(b.nameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(b.vatNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(REPLACE(b.contactPerson, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(TRIM(:#{#filter.term}), ' ', ''), '%')) OR " +
            "           LOWER(REPLACE(b.phone, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(TRIM(:#{#filter.term}), ' ', ''), '%')) OR " +
            "           LOWER(b.email) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    )")
    Page<BillerEntity> findAllByFilter(@Param("filter") BillerRetrieveRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            SUM(COALESCE(ref.count, 0)) AS count
        FROM (
            SELECT :billerId AS billerId, COUNT(*) AS count FROM purchases_order po WHERE po.biller_id = :billerId
            UNION ALL
            SELECT :billerId AS billerId, COUNT(*) AS count FROM purchases p        WHERE p.biller_id = :billerId
            UNION ALL
            SELECT :billerId AS billerId, COUNT(*) AS count FROM sales s            WHERE s.biller_id = :billerId
            UNION ALL
            SELECT :billerId AS billerId, COUNT(*) AS count FROM suspended_bills sp WHERE sp.biller_id = :billerId
            UNION ALL
            SELECT :billerId AS billerId, COUNT(*) AS count FROM adjustments aj     WHERE aj.biller_id = :billerId
            UNION ALL
            SELECT :billerId AS billerId, COUNT(*) AS count FROM transfers tr       WHERE tr.from_biller = :billerId OR tr.to_biller = :billerId
            UNION ALL
            SELECT :billerId AS billerId, COUNT(*) AS count FROM stock_counts sc    WHERE sc.biller_id = :billerId
            UNION ALL
            SELECT :billerId AS billerId, COUNT(*) AS count FROM expenses xp        WHERE xp.biller_id = :billerId
            UNION ALL
            SELECT :billerId AS billerId, COUNT(*) AS count FROM payments pay       WHERE pay.biller_id = :billerId
            UNION ALL
            SELECT :billerId AS billerId, COUNT(*) AS count FROM v_users u          WHERE FIND_IN_SET(:billerId, u.billers)
        ) ref
    """, nativeQuery = true)
    long countReferences(Long billerId);
}
