package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.taxRate.TaxDeclareRetrieveRequest;
import kh.com.csx.posapi.entity.TaxDeclarationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface TaxDeclarationRepository extends JpaRepository<TaxDeclarationEntity, Long> {
    @Query("""
        SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
        FROM TaxEntity t
        WHERE
            (
                (:taxDeclareId IS NULL AND t.transactionId = :transactionId) OR
                (:taxDeclareId IS NOT NULL AND t.id != :taxDeclareId AND t.transactionId = :transactionId)
            ) AND
            t.transaction = :type
    """)
    boolean checkTransaction(String type, Long transactionId, Long taxDeclareId);

    @Query("""
        SELECT
            COALESCE(MAX(CAST(SUBSTRING_INDEX(t.taxReferenceNo, '/', -1) AS int)), 0)
        FROM TaxEntity t
        WHERE
            t.transaction = :type AND
            t.taxReferenceNo LIKE CONCAT(:prefixYear, '%')
    """)
    int getLastTransactionRef(String type, String prefixYear);

    @Query(value = """
        SELECT t
        FROM TaxDeclarationEntity t
        LEFT JOIN BillerEntity b ON b.id = t.billerId
        LEFT JOIN UserEntity u ON u.userId = t.createdBy
        LEFT JOIN EmployeeEntity e ON e.id = u.employeeId
        WHERE 1=1 AND
            (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR t.createdBy = :#{#filter.user}) AND
            (:#{#filter.bIds} IS NULL OR t.billerId IN :#{#filter.bIds}) AND
            (:#{#filter.month} IS NULL OR :#{#filter.month} = '' OR MONTH(t.date) = :#{#filter.month}) AND
            (:#{#filter.year} IS NULL OR :#{#filter.year} = '' OR YEAR(t.date) = :#{#filter.year}) AND
            (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR t.id = :#{#filter.id}) AND
            (:#{#filter.type} IS NULL OR :#{#filter.type} = '' OR t.type = :#{#filter.type}) AND
            (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR t.billerId = :#{#filter.billerId}) AND
            (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR t.createdBy = :#{#filter.createdBy}) AND
            (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR t.updatedBy = :#{#filter.updatedBy}) AND
            (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (t.date BETWEEN :#{#filter.start} AND :#{#filter.end})) AND
            (
                (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') OR
                (
                    LOWER(FUNCTION('DATE_FORMAT', t.date, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CONCAT(b.companyEn, ' (', b.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(t.type) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CASE WHEN e.id IS NULL THEN u.username ELSE CONCAT(COALESCE(e.firstName, ''), ' ', COALESCE(e.lastName, '')) END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                )
            )
    """)
    Page<TaxDeclarationEntity> findAllByFilter(@Param("filter") TaxDeclareRetrieveRequest filter, Pageable pageable);
}
