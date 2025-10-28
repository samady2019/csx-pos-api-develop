package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.purchase.PurchaseRetrieveRequest;
import kh.com.csx.posapi.dto.report.purchaseReport.DailyPurchaseReportResponse;
import kh.com.csx.posapi.dto.report.purchaseReport.GeneralPurchaseReportResponse;
import kh.com.csx.posapi.dto.report.purchaseReport.MonthlyPurchaseReportResponse;
import kh.com.csx.posapi.entity.PurchaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<PurchaseEntity, Long> {
    Optional<PurchaseEntity> findById(Long id);

    boolean existsByReferenceNo(String referenceNo);

    @Query("SELECT COUNT(p) > 0 FROM PurchaseEntity p WHERE p.referenceNo = :referenceNo AND p.status = 'returned'")
    boolean existsByReturnReferenceNo(String referenceNo);

    boolean existsByReferenceNoAndIdNot(String referenceNo, Long id);

    @Modifying
    @Transactional
    @Query(value = "CALL UpdatePurchaseOrderStatus(:purchaseOrderId)", nativeQuery = true)
    void updatePurchaseOrderStatus(Long purchaseOrderId);

//    @Query(value =
//            "SELECT p.* FROM purchases p " +
//                    "LEFT JOIN purchases_order po ON p.purchase_order_id = po.id " +
//                    "WHERE 1=1 " +
//                    "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR p.id = :#{#filter.id}) " +
//                    "AND (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (p.date BETWEEN :#{#filter.startDate} AND :#{#filter.endDate})) " +
//                    "AND (:#{#filter.referenceNo} IS NULL OR :#{#filter.referenceNo} = '' OR p.reference_no = :#{#filter.referenceNo}) " +
//                    "AND (:#{#filter.poReferenceNo} IS NULL OR :#{#filter.poReferenceNo} = '' OR po.reference_no = :#{#filter.poReferenceNo}) " +
//                    "AND (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR p.biller_id = :#{#filter.billerId}) " +
//                    "AND (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR p.warehouse_id = :#{#filter.warehouseId}) " +
//                    "AND (:#{#filter.supplierId} IS NULL OR :#{#filter.supplierId} = '' OR p.supplier_id = :#{#filter.supplierId}) " +
//                    "AND (:#{#filter.status} IS NULL OR :#{#filter.status} = '' OR p.status = :#{#filter.status}) " +
//                    "AND (:#{#filter.paymentStatus} IS NULL OR :#{#filter.paymentStatus} = '' OR p.payment_status = :#{#filter.paymentStatus}) " +
//                    "AND (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR p.created_by = :#{#filter.createdBy}) " +
//                    "AND (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR p.updated_by = :#{#filter.updatedBy}) "
//            , nativeQuery = true)
//    List<PurchaseEntity> findAllByFilter(@Param("filter") PurchaseRetrieveRequest filter);

    @Query(value =
            "SELECT p FROM PurchaseEntity p " +
            "LEFT JOIN PurchaseOrderEntity po ON p.purchaseOrderId = po.id " +
            "LEFT JOIN BillerEntity b ON b.id = p.billerId " +
            "LEFT JOIN WarehouseEntity w ON w.id = p.warehouseId " +
            "LEFT JOIN SupplierEntity s ON s.id = p.supplierId " +
            "LEFT JOIN UserEntity u ON u.userId = p.createdBy " +
            "LEFT JOIN EmployeeEntity e ON e.id = u.employeeId " +
            "LEFT JOIN TaxEntity txd ON txd.transaction = 'purchase' AND txd.transactionId = p.id " +
            "WHERE 1=1 " +
            "AND (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR p.createdBy = :#{#filter.user}) " +
            "AND (:#{#filter.bIds} IS NULL OR p.billerId IN :#{#filter.bIds}) " +
            "AND (:#{#filter.whIds} IS NULL OR p.warehouseId IN :#{#filter.whIds}) " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR p.id = :#{#filter.id}) " +
            "AND (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (p.date BETWEEN :#{#filter.start} AND :#{#filter.end})) " +
            "AND (:#{#filter.referenceNo} IS NULL OR :#{#filter.referenceNo} = '' OR p.referenceNo = :#{#filter.referenceNo}) " +
            "AND (:#{#filter.poReferenceNo} IS NULL OR :#{#filter.poReferenceNo} = '' OR po.referenceNo = :#{#filter.poReferenceNo}) " +
            "AND (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR p.billerId = :#{#filter.billerId}) " +
            "AND (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR p.warehouseId = :#{#filter.warehouseId}) " +
            "AND (:#{#filter.supplierId} IS NULL OR :#{#filter.supplierId} = '' OR p.supplierId = :#{#filter.supplierId}) " +
            "AND (:#{#filter.status} IS NULL OR :#{#filter.status} = '' OR p.status = :#{#filter.status}) " +
            "AND (:#{#filter.paymentStatus} IS NULL OR :#{#filter.paymentStatus} = '' OR p.paymentStatus = :#{#filter.paymentStatus}) " +
            "AND (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR p.createdBy = :#{#filter.createdBy}) " +
            "AND (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR p.updatedBy = :#{#filter.updatedBy}) " +
            "AND ((:#{#filter.taxDeclare} IS NULL OR :#{#filter.taxDeclare} = false) OR (:#{#filter.taxDeclare} = true AND txd.id IS NULL)) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(FUNCTION('DATE_FORMAT', p.date, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(p.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(po.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(b.companyEn, ' (', b.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(w.name, ' (', w.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.companyEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.nameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(p.status) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(p.paymentStatus) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(p.note) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CASE WHEN e.id IS NULL THEN u.username ELSE CONCAT(COALESCE(e.firstName, ''), ' ', COALESCE(e.lastName, '')) END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    ) ")
    Page<PurchaseEntity> findAllByFilter(@Param("filter") PurchaseRetrieveRequest filter, Pageable pageable);

    @Query(value = """
        SELECT CASE WHEN p.quantity > 0 THEN 1 ELSE 0 END
        FROM (
            SELECT pi.purchase_id, COALESCE(SUM(pi.quantity), 0) - Abs(COALESCE(SUM(pii.quantity), 0)) AS quantity
            FROM purchase_items pi
            LEFT JOIN (
                SELECT pi.purchase_item_id, SUM(pi.quantity) AS quantity
                FROM purchase_items pi
                WHERE pi.purchase_item_id IS NOT NULL
                GROUP BY pi.purchase_item_id
            ) pii ON pii.purchase_item_id = pi.id
            WHERE pi.purchase_id = :id
            GROUP BY pi.purchase_id
        ) p
    """, nativeQuery = true)
    Integer checkReturn(Long id);

    @Query(value = """
        SELECT (pi.quantity - Abs(COALESCE(pri.quantity, 0)) - Abs(:quantity)) AS quantity
        FROM purchase_items pi
        LEFT JOIN (
            SELECT pi.purchase_item_id, SUM(pi.quantity) AS quantity
            FROM purchase_items pi
            WHERE pi.purchase_item_id = :purchase_item_id
            GROUP BY pi.purchase_item_id
        ) pri ON pri.purchase_item_id = pi.id
        WHERE
            pi.id = :purchase_item_id AND
            pi.product_id = :product_id AND
            (
                (:expiry IS NULL AND pi.expiry IS NULL) OR 
                (:expiry IS NOT NULL AND pi.expiry = :expiry)
            )
    """, nativeQuery = true)
    Double returnBalanceQuantity(Long purchase_item_id, Long product_id, LocalDate expiry, Double quantity);

    @Query("""
        SELECT SUM(pi.unitQuantity) AS totalQuantity
        FROM PurchaseEntity p
        INNER JOIN PurchaseItemEntity pi ON pi.purchaseId = p.id
        WHERE p.id = :id
    """)
    Double getPurchaseTotalQuantity(Long id);
}
