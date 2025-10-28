package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.customer.CustomerRetrieveRequest;
import kh.com.csx.posapi.entity.CustomerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    boolean existsById(Long id);
    boolean existsByContactPerson(String contactPerson);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);

    @Query(value = "SELECT c FROM CustomerEntity c WHERE " +
            "( " +
            "    LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "    LOWER(c.nameKh) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "    LOWER(c.companyEn) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "    LOWER(c.companyKh) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "    LOWER(REPLACE(c.contactPerson, ' ', '')) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "    LOWER(REPLACE(c.phone, ' ', '')) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "    LOWER(c.email) LIKE LOWER(CONCAT('%', :term, '%')) " +
            ") " +
            "ORDER BY c.nameEn ASC LIMIT 20 ")
    List<CustomerEntity> findByTerm(String term);

    @Query("SELECT c FROM CustomerEntity c WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR c.id = :#{#filter.id}) " +
            "AND (:#{#filter.nameEn} IS NULL OR :#{#filter.nameEn} = '' OR LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :#{#filter.nameEn}, '%'))) " +
            "AND (:#{#filter.nameKh} IS NULL OR :#{#filter.nameKh} = '' OR LOWER(c.nameKh) LIKE LOWER(CONCAT('%', :#{#filter.nameKh}, '%'))) " +
            "AND (:#{#filter.companyEn} IS NULL OR :#{#filter.companyEn} = '' OR LOWER(c.companyEn) LIKE LOWER(CONCAT('%', :#{#filter.companyEn}, '%'))) " +
            "AND (:#{#filter.companyKh} IS NULL OR :#{#filter.companyKh} = '' OR LOWER(c.companyKh) LIKE LOWER(CONCAT('%', :#{#filter.companyKh}, '%'))) " +
            "AND (:#{#filter.gender} IS NULL OR :#{#filter.gender} = '' OR LOWER(:#{#filter.gender}) IN ('m', 'male') AND LOWER(c.gender) = 'male' " +
            "OR LOWER(:#{#filter.gender}) IN ('f', 'female') AND LOWER(c.gender) = 'female') " +
            "AND (:#{#filter.contactPerson} IS NULL OR :#{#filter.contactPerson} = '' OR LOWER(c.contactPerson) LIKE LOWER(CONCAT('%', :#{#filter.contactPerson}, '%'))) " +
            "AND (:#{#filter.email} IS NULL OR :#{#filter.email} = '' OR LOWER(c.email) LIKE LOWER(CONCAT('%', :#{#filter.email}, '%'))) " +
            "AND (:#{#filter.phone} IS NULL OR :#{#filter.phone} = '' OR c.phone LIKE CONCAT('%', :#{#filter.phone}, '%'))")
    List<CustomerEntity> findListByFilter(@Param("filter") CustomerRetrieveRequest filter);

    @Query("SELECT c FROM CustomerEntity c " +
            "LEFT JOIN CustomerGroupEntity cg ON cg.id = c.customerGroupId " +
            "WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR c.id = :#{#filter.id}) " +
            "AND (:#{#filter.nameEn} IS NULL OR LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :#{#filter.nameEn}, '%'))) " +
            "AND (:#{#filter.nameKh} IS NULL OR LOWER(c.nameKh) LIKE LOWER(CONCAT('%', :#{#filter.nameKh}, '%'))) " +
            "AND (:#{#filter.companyEn} IS NULL OR LOWER(c.companyEn) LIKE LOWER(CONCAT('%', :#{#filter.companyEn}, '%'))) " +
            "AND (:#{#filter.companyKh} IS NULL OR LOWER(c.companyKh) LIKE LOWER(CONCAT('%', :#{#filter.companyKh}, '%'))) " +
            "AND (:#{#filter.vatNo} IS NULL OR :#{#filter.vatNo} = '' OR LOWER(c.vatNo) LIKE LOWER(CONCAT('%', :#{#filter.vatNo}, '%'))) " +
            "AND (:#{#filter.gender} IS NULL OR LOWER(:#{#filter.gender}) IN ('m', 'male') AND LOWER(c.gender) = 'male' " +
            "OR LOWER(:#{#filter.gender}) IN ('f', 'female') AND LOWER(c.gender) = 'female') " +
            "AND (:#{#filter.contactPerson} IS NULL OR LOWER(c.contactPerson) LIKE LOWER(CONCAT('%', :#{#filter.contactPerson}, '%'))) " +
            "AND (:#{#filter.phone} IS NULL OR c.phone LIKE CONCAT('%', :#{#filter.phone}, '%'))  " +
            "AND (:#{#filter.email} IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :#{#filter.email}, '%'))) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(c.nameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(c.nameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(c.companyEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(c.companyKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(c.vatNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(c.gender) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(REPLACE(c.contactPerson, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(TRIM(:#{#filter.term}), ' ', ''), '%')) OR " +
            "           LOWER(REPLACE(c.phone, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(TRIM(:#{#filter.term}), ' ', ''), '%')) OR " +
            "           LOWER(c.email) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(c.addressKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(c.addressEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(cg.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    )")
    Page<CustomerEntity> findAllByFilter(@Param("filter") CustomerRetrieveRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            SUM(COALESCE(ref.count, 0)) AS count
        FROM (
            SELECT :id AS id, COUNT(*) AS count FROM sales s            WHERE s.customer_id = :id
            UNION ALL
            SELECT :id AS id, COUNT(*) AS count FROM suspended_bills sp WHERE sp.customer_id = :id
        ) ref
    """, nativeQuery = true)
    long countReferences(Long id);
}
