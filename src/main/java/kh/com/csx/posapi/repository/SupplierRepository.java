package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.supplier.SupplierRetrieveRequest;
import kh.com.csx.posapi.entity.SupplierEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<SupplierEntity, Long> {
    Optional<SupplierEntity> findById(Long id);
    boolean existsByContactPerson(String contactPerson);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);

    @Query(value = "SELECT s FROM SupplierEntity s WHERE " +
            "( " +
            "    LOWER(s.companyEn) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "    LOWER(s.companyKh) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "    LOWER(s.nameEn) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "    LOWER(s.nameKh) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "    LOWER(REPLACE(s.contactPerson, ' ', '')) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "    LOWER(REPLACE(s.phone, ' ', '')) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "    LOWER(s.email) LIKE LOWER(CONCAT('%', :term, '%')) " +
            ") " +
            "ORDER BY s.companyEn ASC LIMIT 20 ")
    List<SupplierEntity> findByTerm(String term);

    @Query(value = "SELECT s FROM SupplierEntity s WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR s.id = :#{#filter.id}) " +
            "AND (:#{#filter.companyEn} IS NULL OR :#{#filter.companyEn} = '' OR s.companyEn LIKE CONCAT('%', :#{#filter.companyEn}, '%')) " +
            "AND (:#{#filter.companyKh} IS NULL OR :#{#filter.companyKh} = '' OR s.companyKh LIKE CONCAT('%', :#{#filter.companyKh}, '%')) " +
            "AND (:#{#filter.nameEn} IS NULL OR :#{#filter.nameEn} = '' OR s.nameEn LIKE CONCAT('%', :#{#filter.nameEn}, '%')) " +
            "AND (:#{#filter.nameKh} IS NULL OR :#{#filter.nameKh} = '' OR s.nameKh LIKE CONCAT('%', :#{#filter.nameKh}, '%')) " +
            "AND (:#{#filter.gender} IS NULL OR :#{#filter.gender} = '' OR s.gender = :#{#filter.gender}) " +
            "AND (:#{#filter.vatNo} IS NULL OR :#{#filter.vatNo} = '' OR s.vatNo = :#{#filter.vatNo}) " +
            "AND (:#{#filter.contactPerson} IS NULL OR :#{#filter.contactPerson} = '' OR s.contactPerson LIKE CONCAT('%', :#{#filter.contactPerson}, '%')) " +
            "AND (:#{#filter.phone} IS NULL OR :#{#filter.phone} = '' OR s.phone LIKE CONCAT('%', :#{#filter.phone}, '%')) " +
            "AND (:#{#filter.email} IS NULL OR :#{#filter.email} = '' OR s.email LIKE CONCAT('%', :#{#filter.email}, '%')) ")
    List<SupplierEntity> findListByFilter(@Param("filter") SupplierRetrieveRequest filter);

    @Query(value = "SELECT s FROM SupplierEntity s WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR s.id = :#{#filter.id}) " +
            "AND (:#{#filter.companyEn} IS NULL OR :#{#filter.companyEn} = '' OR s.companyEn LIKE CONCAT('%', :#{#filter.companyEn}, '%')) " +
            "AND (:#{#filter.companyKh} IS NULL OR :#{#filter.companyKh} = '' OR s.companyKh LIKE CONCAT('%', :#{#filter.companyKh}, '%')) " +
            "AND (:#{#filter.nameEn} IS NULL OR :#{#filter.nameEn} = '' OR s.nameEn LIKE CONCAT('%', :#{#filter.nameEn}, '%')) " +
            "AND (:#{#filter.nameKh} IS NULL OR :#{#filter.nameKh} = '' OR s.nameKh LIKE CONCAT('%', :#{#filter.nameKh}, '%')) " +
            "AND (:#{#filter.gender} IS NULL OR :#{#filter.gender} = '' OR s.gender = :#{#filter.gender}) " +
            "AND (:#{#filter.vatNo} IS NULL OR :#{#filter.vatNo} = '' OR s.vatNo = :#{#filter.vatNo}) " +
            "AND (:#{#filter.contactPerson} IS NULL OR :#{#filter.contactPerson} = '' OR s.contactPerson LIKE CONCAT('%', :#{#filter.contactPerson}, '%')) " +
            "AND (:#{#filter.phone} IS NULL OR :#{#filter.phone} = '' OR s.phone LIKE CONCAT('%', :#{#filter.phone}, '%')) " +
            "AND (:#{#filter.email} IS NULL OR :#{#filter.email} = '' OR s.email LIKE CONCAT('%', :#{#filter.email}, '%')) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(s.companyEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.companyKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.nameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.nameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.vatNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.gender) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(REPLACE(s.contactPerson, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(TRIM(:#{#filter.term}), ' ', ''), '%')) OR " +
            "           LOWER(REPLACE(s.phone, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(TRIM(:#{#filter.term}), ' ', ''), '%')) OR " +
            "           LOWER(s.email) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.addressKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.addressEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    )")
    Page<SupplierEntity> findAllByFilter(@Param("filter") SupplierRetrieveRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            SUM(COALESCE(ref.count, 0)) AS count
        FROM (
            SELECT :id AS id, COUNT(*) AS count FROM purchases p        WHERE p.supplier_id = :id
            UNION ALL
            SELECT :id AS id, COUNT(*) AS count FROM purchases_order po WHERE po.supplier_id = :id
        ) ref
    """, nativeQuery = true)
    long countReferences(Long id);
}
