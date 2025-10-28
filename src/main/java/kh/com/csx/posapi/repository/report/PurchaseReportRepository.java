package kh.com.csx.posapi.repository.report;

import jakarta.persistence.Tuple;
import kh.com.csx.posapi.dto.report.purchaseReport.DailyPurchaseReportRequest;
import kh.com.csx.posapi.dto.report.purchaseReport.GeneralPurchaseReportRequest;
import kh.com.csx.posapi.dto.report.purchaseReport.MonthlyPurchaseReportRequest;
import kh.com.csx.posapi.entity.PurchaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseReportRepository extends JpaRepository<PurchaseEntity, Long> {
    @Query("SELECT " +
            "CAST(p.date AS java.time.LocalDate) AS date, " +
            "p.billerId AS billerId, " +
            "p.warehouseId AS warehouseId, " +
            "SUM(p.total) AS total, " +
            "SUM(p.orderDiscount) AS discount, " +
            "SUM(p.productTax) AS productTax, " +
            "SUM(p.orderTax) AS orderTax, " +
            "SUM(p.shipping) AS shipping, " +
            "SUM(p.grandTotal) AS grandTotal, " +
            "SUM(p.paid) AS paid, " +
            "COUNT(p) AS totalPurchases, " +
            "p.createdBy AS createdBy, " +
            "p.updatedBy AS updatedBy " +
            "FROM PurchaseEntity p " +
            "WHERE p.date BETWEEN :startDate AND :endDate " +
            "AND (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR p.createdBy = :#{#filter.user}) " +
            "AND (:#{#filter.bIds} IS NULL OR p.billerId IN :#{#filter.bIds}) " +
            "AND (:#{#filter.whIds} IS NULL OR p.warehouseId IN :#{#filter.whIds}) " +
            "AND p.status IN ('partial', 'completed') " +
            "AND (:#{#filter.billerId} IS NULL OR p.billerId = :#{#filter.billerId}) " +
            "AND (:#{#filter.warehouseId} IS NULL OR p.warehouseId = :#{#filter.warehouseId}) " +
            "AND (:#{#filter.createdBy} IS NULL OR p.createdBy = :#{#filter.createdBy}) " +
            "AND (:#{#filter.updatedBy} IS NULL OR p.updatedBy = :#{#filter.updatedBy}) " +
            "GROUP BY CAST(p.date AS java.time.LocalDate)")
    List<Tuple> findDailyPurchaseReport(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("filter") DailyPurchaseReportRequest filter);

    @Query("SELECT " +
            "YEAR(p.date) AS year, " +
            "MONTH(p.date) AS month, " +
            "p.billerId AS billerId, " +
            "p.warehouseId AS warehouseId, " +
            "SUM(p.total) AS total, " +
            "SUM(p.orderDiscount) AS discount, " +
            "SUM(p.productTax) AS productTax, " +
            "SUM(p.orderTax) AS orderTax, " +
            "SUM(p.shipping) AS shipping, " +
            "SUM(p.grandTotal) AS grandTotal, " +
            "SUM(p.paid) AS paid, " +
            "COUNT(p) AS totalPurchases, " +
            "p.createdBy AS createdBy, " +
            "p.updatedBy AS updatedBy " +
            "FROM PurchaseEntity p " +
            "WHERE YEAR(p.date) = :year " +
            "AND p.status IN ('partial', 'completed') " +
            "AND (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR p.createdBy = :#{#filter.user}) " +
            "AND (:#{#filter.bIds} IS NULL OR p.billerId IN :#{#filter.bIds}) " +
            "AND (:#{#filter.whIds} IS NULL OR p.warehouseId IN :#{#filter.whIds}) " +
            "AND (:#{#filter.billerId} IS NULL OR p.billerId = :#{#filter.billerId}) " +
            "AND (:#{#filter.warehouseId} IS NULL OR p.warehouseId = :#{#filter.warehouseId}) " +
            "AND (:#{#filter.createdBy} IS NULL OR p.createdBy = :#{#filter.createdBy}) " +
            "AND (:#{#filter.updatedBy} IS NULL OR p.updatedBy = :#{#filter.updatedBy}) " +
            "GROUP BY YEAR(p.date), MONTH(p.date)")
    List<Tuple> findMonthlyPurchaseReport(@Param("year") int year, @Param("filter") MonthlyPurchaseReportRequest filter);

    @Query("SELECT " +
            "p.id AS id, " +
            "p.purchaseOrderId AS purchaseOrderId, " +
            "p.date AS date, " +
            "p.referenceNo AS referenceNo, " +
            "po.referenceNo AS poReferenceNo, " +
            "CONCAT(b.companyEn, ' (', b.code, ')') AS biller, " +
            "w.name AS warehouse, " +
            "s.companyEn AS supplier, " +
            "p.total AS total, " +
            "p.shipping AS shipping, " +
            "p.orderDiscount AS orderDiscount, " +
            "p.orderDiscountId AS orderDiscountId, " +
            "p.orderTax AS orderTax, " +
            "t.name AS orderTaxId, " +
            "p.grandTotal AS grandTotal, " +
            "p.paid AS paid, " +
            "p.grandTotal - p.paid AS balance, " +
            "p.status AS status, " +
            "p.paymentStatus AS paymentStatus, " +
            "p.note AS note, " +
            "p.attachment AS attachment, " +
            "COALESCE(CONCAT(e.firstName, ' ', e.lastName), u.username) AS createdBy " +
            "FROM PurchaseEntity p " +
            "LEFT JOIN p.biller b " +
            "LEFT JOIN p.warehouse w " +
            "LEFT JOIN p.supplier s " +
            "INNER JOIN (" +
                "SELECT _pi.purchaseId AS purchaseId FROM PurchaseItemEntity _pi " +
                "WHERE (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR _pi.productId = :#{#filter.productId}) " +
                "GROUP BY _pi.purchaseId " +
            ") pi ON pi.purchaseId = p.id " +
            "LEFT JOIN p.purchaseOrder po " +
            "LEFT JOIN TaxRateEntity t ON t.id = p.orderTaxId " +
            "LEFT JOIN UserEntity u ON u.userId = p.createdBy " +
            "LEFT JOIN EmployeeEntity e ON e.id = u.employeeId " +
            "WHERE (:startDate IS NULL OR p.date >= :startDate) " +
            "AND (:endDate IS NULL OR p.date <= :endDate) " +
            "AND (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR p.createdBy = :#{#filter.user}) " +
            "AND (:#{#filter.bIds} IS NULL OR p.billerId IN :#{#filter.bIds}) " +
            "AND (:#{#filter.whIds} IS NULL OR p.warehouseId IN :#{#filter.whIds}) " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR p.id = :#{#filter.id}) " +
            "AND (:#{#filter.purchaseOrderId} IS NULL OR p.purchaseOrderId = :#{#filter.purchaseOrderId}) " +
            "AND (:#{#filter.referenceNo} IS NULL OR :#{#filter.referenceNo} = '' OR p.referenceNo = :#{#filter.referenceNo}) " +
            "AND (:#{#filter.poReferenceNo} IS NULL OR :#{#filter.poReferenceNo} = '' OR po.referenceNo = :#{#filter.poReferenceNo}) " +
            "AND (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR p.billerId = :#{#filter.billerId}) " +
            "AND (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR p.warehouseId = :#{#filter.warehouseId}) " +
            "AND (:#{#filter.supplierId} IS NULL OR :#{#filter.supplierId} = '' OR p.supplierId = :#{#filter.supplierId}) " +
            "AND (:#{#filter.status} IS NULL OR :#{#filter.status} = '' OR p.status = :#{#filter.status}) " +
            "AND (:#{#filter.paymentStatus} IS NULL OR :#{#filter.paymentStatus} = '' OR p.paymentStatus = :#{#filter.paymentStatus}) " +
            "AND (:#{#filter.createdBy} IS NULL OR p.createdBy = :#{#filter.createdBy}) " +
            "AND (:#{#filter.updatedBy} IS NULL OR p.updatedBy = :#{#filter.updatedBy}) " +
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
    Page<Tuple> findGeneralPurchaseReport(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("filter") GeneralPurchaseReportRequest filter, Pageable pageable);
}
