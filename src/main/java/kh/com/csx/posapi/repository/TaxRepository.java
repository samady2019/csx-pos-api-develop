package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.taxRate.TaxDeclareRetrieveRequest;
import kh.com.csx.posapi.entity.TaxEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaxRepository extends JpaRepository<TaxEntity, Long> {
    List<TaxEntity> findByTaxDeclarationId(Long taxDeclarationId);
    TaxEntity findByTransactionAndTransactionId(String transaction, Long transactionId);

    @Modifying
    @Query("DELETE FROM TaxEntity t WHERE t.taxDeclarationId = :taxDeclarationId")
    void deleteByTaxDeclarationId(Long taxDeclarationId);

    @Query(value = """
        SELECT t
        FROM TaxEntity t
        INNER JOIN TaxDeclarationEntity td ON td.id = t.taxDeclarationId
        LEFT JOIN SaleEntity s ON s.id = t.transactionId AND t.transaction = 'sale'
        LEFT JOIN CustomerEntity cus ON cus.id = s.customerId
        LEFT JOIN PurchaseEntity p ON p.id = t.transactionId AND t.transaction = 'purchase'
        LEFT JOIN SupplierEntity sup ON sup.id = p.supplierId
        LEFT JOIN BillerEntity b ON b.id = td.billerId
        LEFT JOIN UserEntity u ON u.userId = td.createdBy
        LEFT JOIN EmployeeEntity e ON e.id = u.employeeId
        WHERE 1=1 AND
            (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR td.createdBy = :#{#filter.user}) AND
            (:#{#filter.bIds} IS NULL OR td.billerId IN :#{#filter.bIds}) AND
            (:#{#filter.month} IS NULL OR :#{#filter.month} = '' OR MONTH(td.date) = :#{#filter.month}) AND
            (:#{#filter.year} IS NULL OR :#{#filter.year} = '' OR YEAR(td.date) = :#{#filter.year}) AND
            (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR td.id = :#{#filter.id}) AND
            (:#{#filter.type} IS NULL OR :#{#filter.type} = '' OR td.type = :#{#filter.type}) AND
            (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR td.billerId = :#{#filter.billerId}) AND
            (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR td.createdBy = :#{#filter.createdBy}) AND
            (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR td.updatedBy = :#{#filter.updatedBy}) AND
            (
                :#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR
                (
                    (s.id IS NOT NULL AND s.date BETWEEN :#{#filter.start} AND :#{#filter.end}) OR
                    (p.id IS NOT NULL AND p.date BETWEEN :#{#filter.start} AND :#{#filter.end})
                )
            ) AND
            (
                (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') OR
                (
                    LOWER(FUNCTION('DATE_FORMAT', s.date, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(FUNCTION('DATE_FORMAT', p.date, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(t.taxReferenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(t.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(t.companyEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(t.companyKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(t.nameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(t.nameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(REPLACE(t.phone, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(TRIM(:#{#filter.term}), ' ', ''), '%')) OR
                    LOWER(t.email) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(t.vatNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(t.note) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CONCAT(b.companyEn, ' (', b.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CONCAT(b.companyKh, ' (', b.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(td.type) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CASE WHEN e.id IS NULL THEN u.username ELSE CONCAT(COALESCE(e.firstName, ''), ' ', COALESCE(e.lastName, '')) END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                )
            )
    """)
    Page<TaxEntity> findAllByFilter(@Param("filter") TaxDeclareRetrieveRequest filter, Pageable pageable);
}
