package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.sale.SaleRetrieveRequest;
import kh.com.csx.posapi.entity.SaleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<SaleEntity, Long> {

    Optional<SaleEntity> findById(Long id);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM sales WHERE reference_no = :referenceNo AND pos = 0", nativeQuery = true)
    boolean existsByReferenceNo(String referenceNo);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM sales WHERE reference_no = :referenceNo AND pos = 1", nativeQuery = true)
    boolean existsByReferenceNoPOS(@Param("referenceNo") String referenceNo);

    @Modifying
    @Transactional
    @Query(value = "CALL UpdateSaleOrderStatus(:saleOrderId)", nativeQuery = true)
    void updateSaleOrderStatus(Long saleOrderId);

    @Modifying
    @Transactional
    @Query(value = "CALL UpdateQuoteStatus(:quoteId)", nativeQuery = true)
    void updateQuoteStatus(Long quoteId);

    @Query(value = "SELECT COALESCE(MAX(wait_number), 0) + 1 FROM sales WHERE pos = 1 AND biller_id = :billerId AND DATE(date) = CURRENT_DATE()", nativeQuery = true)
    int getWaitNumber(Long billerId);

    @Query(value = "SELECT s FROM SaleEntity s " +
            "LEFT JOIN BillerEntity b ON b.id = s.billerId " +
            "LEFT JOIN WarehouseEntity w ON w.id = s.warehouseId " +
            "LEFT JOIN CustomerEntity c ON c.id = s.customerId " +
            "LEFT JOIN UserEntity u ON u.userId = s.createdBy " +
            "LEFT JOIN EmployeeEntity e ON e.id = u.employeeId " +
            "LEFT JOIN TaxEntity txd ON txd.transaction = 'sale' AND txd.transactionId = s.id " +
            "WHERE 1=1 " +
            "AND (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR s.createdBy = :#{#filter.user}) " +
            "AND (:#{#filter.bIds} IS NULL OR s.billerId IN :#{#filter.bIds}) " +
            "AND (:#{#filter.whIds} IS NULL OR s.warehouseId IN :#{#filter.whIds}) " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR s.id = :#{#filter.id}) " +
            "AND (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (s.date BETWEEN :#{#filter.start} AND :#{#filter.end})) " +
            "AND (:#{#filter.referenceNo} IS NULL OR :#{#filter.referenceNo} = '' OR s.referenceNo = :#{#filter.referenceNo}) " +
            "AND (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR s.billerId = :#{#filter.billerId}) " +
            "AND (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR s.warehouseId = :#{#filter.warehouseId}) " +
            "AND (:#{#filter.customerId} IS NULL OR :#{#filter.customerId} = '' OR s.customerId = :#{#filter.customerId}) " +
            "AND (:#{#filter.status} IS NULL OR :#{#filter.status} = '' OR s.status = :#{#filter.status}) " +
            "AND (:#{#filter.paymentStatus} IS NULL OR :#{#filter.paymentStatus} = '' OR s.paymentStatus = :#{#filter.paymentStatus}) " +
            "AND (:#{#filter.deliveryStatus} IS NULL OR :#{#filter.deliveryStatus} = '' OR s.deliveryStatus = :#{#filter.deliveryStatus}) " +
            "AND (:#{#filter.salesmanBy} IS NULL OR :#{#filter.salesmanBy} = '' OR s.salesmanBy = :#{#filter.salesmanBy}) " +
            "AND (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR s.createdBy = :#{#filter.createdBy}) " +
            "AND (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR s.updatedBy = :#{#filter.updatedBy}) " +
            "AND (:#{#filter.pos} IS NULL OR :#{#filter.pos} = '' OR s.pos = :#{#filter.pos}) " +
            "AND ((:#{#filter.taxDeclare} IS NULL OR :#{#filter.taxDeclare} = false) OR (:#{#filter.taxDeclare} = true AND txd.id IS NULL)) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(FUNCTION('DATE_FORMAT', s.date, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(b.companyEn, ' (', b.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(w.name, ' (', w.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(c.companyEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(c.nameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.status) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.paymentStatus) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.deliveryStatus) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.note) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CASE WHEN e.id IS NULL THEN u.username ELSE CONCAT(COALESCE(e.firstName, ''), ' ', COALESCE(e.lastName, '')) END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    ) ")
    Page<SaleEntity> findAllByFilter(@Param("filter") SaleRetrieveRequest filter, Pageable pageable);

    @Query("""
        SELECT SUM(si.unitQuantity) AS totalQuantity
        FROM SaleEntity s
        INNER JOIN SaleItemEntity si ON si.saleId = s.id
        WHERE s.id = :id
    """)
    Double getSaleTotalQuantity(Long id);

}
