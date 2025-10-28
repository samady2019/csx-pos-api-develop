package kh.com.csx.posapi.repository.report;

import jakarta.persistence.Tuple;
import kh.com.csx.posapi.dto.report.saleReport.*;
import kh.com.csx.posapi.entity.SaleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleReportRepository extends JpaRepository<SaleEntity, Long> {
    @Query("SELECT " +
            "s.id AS id, " +
            "CAST(s.date AS java.time.LocalDate) AS date, " +
            "s.billerId AS billerId, s.warehouseId AS warehouseId, s.customerId AS customerId, " +
            "s.createdBy AS createdBy, s.updatedBy AS updatedBy," +
            "SUM(s.total) AS total, " +
            "SUM(s.orderDiscount) AS discount, " +
            "SUM(s.productTax) AS productTax, " +
            "SUM(s.orderTax) AS orderTax, " +
            "SUM(s.shipping) AS shipping, " +
            "SUM(s.grandTotal) AS grandTotal, " +
            "SUM(s.paid) AS paid, " +
            "COUNT(s) AS totalSales " +
            "FROM SaleEntity s " +
            "WHERE s.date BETWEEN :startDate AND :endDate " +
            "AND (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR s.createdBy = :#{#filter.user}) " +
            "AND (:#{#filter.bIds} IS NULL OR s.billerId IN :#{#filter.bIds}) " +
            "AND (:#{#filter.whIds} IS NULL OR s.warehouseId IN :#{#filter.whIds}) " +
            "AND s.status IN ('partial', 'completed') " +
            "AND (:#{#filter.billerId} IS NULL OR s.billerId = :#{#filter.billerId}) " +
            "AND (:#{#filter.warehouseId} IS NULL OR s.warehouseId = :#{#filter.warehouseId}) " +
            "AND (:#{#filter.createdBy} IS NULL OR s.createdBy = :#{#filter.createdBy}) " +
            "AND (:#{#filter.updatedBy} IS NULL OR s.updatedBy = :#{#filter.updatedBy}) " +
            "GROUP BY CAST(s.date AS java.time.LocalDate)")
    List<Tuple> findDailySalesReport(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("filter") DailySalesReportRequest filter);

    @Query("SELECT " +
            "YEAR(s.date) AS year, " +
            "MONTH(s.date) AS month, " +
            "s.billerId AS billerId, " +
            "s.warehouseId AS warehouseId, " +
            "s.customerId AS customerId, " +
            "s.referenceNo AS referenceNo, " +
            "SUM(s.total) AS total, " +
            "SUM(s.orderDiscount) AS discount, " +
            "SUM(s.productTax) AS productTax, " +
            "SUM(s.orderTax) AS orderTax, " +
            "SUM(s.shipping) AS shipping, " +
            "SUM(s.grandTotal) AS grandTotal, " +
            "SUM(s.paid) AS paid, " +
            "COUNT(s) AS totalSales, " +
            "s.createdBy AS createdBy, " +
            "s.updatedBy AS updatedBy " +
            "FROM SaleEntity s " +
            "WHERE YEAR(s.date) = :year " +
            "AND s.status IN ('partial', 'completed') " +
            "AND (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR s.createdBy = :#{#filter.user}) " +
            "AND (:#{#filter.bIds} IS NULL OR s.billerId IN :#{#filter.bIds}) " +
            "AND (:#{#filter.whIds} IS NULL OR s.warehouseId IN :#{#filter.whIds}) " +
            "AND (:#{#filter.billerId} IS NULL OR s.billerId = :#{#filter.billerId}) " +
            "AND (:#{#filter.warehouseId} IS NULL OR s.warehouseId = :#{#filter.warehouseId}) " +
            "AND (:#{#filter.createdBy} IS NULL OR s.createdBy = :#{#filter.createdBy}) " +
            "AND (:#{#filter.updatedBy} IS NULL OR s.updatedBy = :#{#filter.updatedBy}) " +
            "GROUP BY YEAR(s.date), MONTH(s.date)")
    List<Tuple> findMonthlySalesReport(@Param("year") int year, @Param("filter") MonthlySalesReportRequest filter);

    @Query("SELECT " +
            "s.id AS id, " +
            "s.saleOrderId AS saleOrderId, " +
            "s.date AS date, " +
            "s.referenceNo AS referenceNo, " +
            "'' AS soReferenceNo, " +
            "CONCAT(b.companyEn, ' (', b.code, ')') AS biller, " +
            "w.name AS warehouse, " +
            "cus.nameEn AS customer, " +
            "s.total AS total, " +
            "s.shipping AS shipping, " +
            "s.orderDiscount AS orderDiscount, " +
            "s.orderDiscountId AS orderDiscountId, " +
            "s.orderTax AS orderTax, " +
            "t.name AS orderTaxId, " +
            "s.grandTotal AS grandTotal, " +
            "s.paid AS paid, " +
            "s.grandTotal - s.paid AS balance, " +
            "s.changes AS changes, " +
            "ROUND(ci.cost, 2) AS cost, " +
            "s.status AS status, " +
            "s.paymentStatus AS paymentStatus, " +
            "s.deliveryStatus AS deliveryStatus, " +
            "s.note AS note, " +
            "s.attachment AS attachment, " +
            "COALESCE(CONCAT(e.firstName, ' ', e.lastName), u.username) AS createdBy " +
            "FROM SaleEntity s " +
            "LEFT JOIN s.biller b " +
            "LEFT JOIN s.warehouse w " +
            "LEFT JOIN s.customer cus " +
            "LEFT JOIN TaxRateEntity t ON t.id = s.orderTaxId " +
            "LEFT JOIN UserEntity u ON u.userId = s.createdBy " +
            "LEFT JOIN EmployeeEntity e ON e.id = u.employeeId " +
            "INNER JOIN ( " +
                "SELECT _si.saleId AS saleId, SUM(_si.quantity * _si.baseUnitCost) AS cost FROM SaleItemEntity _si " +
                "GROUP BY _si.saleId " +
            ") ci ON ci.saleId = s.id " +
            "INNER JOIN ( " +
                "SELECT _si.saleId AS saleId FROM SaleItemEntity _si " +
                "WHERE (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR _si.productId = :#{#filter.productId}) " +
                "GROUP BY _si.saleId " +
            ") si ON si.saleId = s.id " +
            "WHERE (:startDate IS NULL OR s.date >= :startDate) " +
            "AND (:endDate IS NULL OR s.date <= :endDate) " +
            "AND (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR s.createdBy = :#{#filter.user}) " +
            "AND (:#{#filter.bIds} IS NULL OR s.billerId IN :#{#filter.bIds}) " +
            "AND (:#{#filter.whIds} IS NULL OR s.warehouseId IN :#{#filter.whIds}) " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR s.id = :#{#filter.id}) " +
            "AND (:#{#filter.referenceNo} IS NULL OR :#{#filter.referenceNo} = '' OR s.referenceNo = :#{#filter.referenceNo}) " +
            "AND (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR s.billerId = :#{#filter.billerId}) " +
            "AND (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR s.warehouseId = :#{#filter.warehouseId}) " +
            "AND (:#{#filter.customerId} IS NULL OR :#{#filter.customerId} = '' OR s.customerId = :#{#filter.customerId}) " +
            "AND (:#{#filter.status} IS NULL OR :#{#filter.status} = '' OR s.status = :#{#filter.status}) " +
            "AND (:#{#filter.paymentStatus} IS NULL OR :#{#filter.paymentStatus} = '' OR s.paymentStatus = :#{#filter.paymentStatus}) " +
            "AND (:#{#filter.deliveryStatus} IS NULL OR :#{#filter.deliveryStatus} = '' OR s.deliveryStatus = :#{#filter.deliveryStatus}) " +
            "AND (:#{#filter.createdBy} IS NULL OR s.createdBy = :#{#filter.createdBy}) " +
            "AND (:#{#filter.updatedBy} IS NULL OR s.updatedBy = :#{#filter.updatedBy}) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(FUNCTION('DATE_FORMAT', s.date, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(b.companyEn, ' (', b.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(w.name, ' (', w.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(cus.companyEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(cus.nameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.status) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.paymentStatus) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.deliveryStatus) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.note) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CASE WHEN e.id IS NULL THEN u.username ELSE CONCAT(COALESCE(e.firstName, ''), ' ', COALESCE(e.lastName, '')) END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    ) ")
    Page<Tuple> findGeneralSalesReport(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("filter") GeneralSalesReportRequest filter, Pageable pageable);

    @Query("SELECT " +
            "p.productId AS productId, " +
            "s.referenceNo AS referenceNo, " +
            "s.date AS date, " +
            "c.nameEn AS customerName, " +
            "p.productCode AS productCode, " +
            "p.productNameEn AS productNameEn, " +
            "p.productNameKh AS productNameKh, " +
            "si.unitPrice AS unitPrice, " +
            "si.unitQuantity AS unitQuantity, " +
            "si.discount AS discount " +
            "FROM SaleEntity s " +
            "JOIN s.items si " +
            "JOIN ProductEntity p ON si.productId = p.productId " +
            "LEFT JOIN BillerEntity b ON b.id = s.billerId " +
            "LEFT JOIN WarehouseEntity w ON w.id = s.warehouseId " +
            "LEFT JOIN CustomerEntity c ON c.id = s.customerId " +
            "LEFT JOIN UserEntity u ON u.userId = s.createdBy " +
            "LEFT JOIN EmployeeEntity e ON e.id = u.employeeId " +
            "WHERE si.itemDiscount > 0 " +
            "AND (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR s.createdBy = :#{#filter.user}) " +
            "AND (:#{#filter.bIds} IS NULL OR s.billerId IN :#{#filter.bIds}) " +
            "AND (:#{#filter.whIds} IS NULL OR s.warehouseId IN :#{#filter.whIds}) " +
            "AND (:#{#filter.referenceNo} IS NULL OR :#{#filter.referenceNo} = '' OR s.referenceNo = :#{#filter.referenceNo}) " +
            "AND (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR s.billerId = :#{#filter.billerId}) " +
            "AND (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR s.warehouseId = :#{#filter.warehouseId}) " +
            "AND (:#{#filter.customerId} IS NULL OR :#{#filter.customerId} = '' OR s.customerId = :#{#filter.customerId}) " +
            "AND s.status = 'completed' " +
            "AND s.date BETWEEN :startDate AND :endDate "+
            "AND (:#{#filter.productId} IS NULL OR p.productId = :#{#filter.productId}) " +
            "AND (:#{#filter.referenceNo} IS NULL OR s.referenceNo = :#{#filter.referenceNo}) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(FUNCTION('DATE_FORMAT', s.date, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(p.productCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(p.productNameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(p.productNameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
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
    Page<Tuple> findSalesDiscountReport(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("filter") SalesDiscountReportRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            cus.id AS customerId,
            cus.companyEn AS companyEn,
            cus.companyKh AS companyKh,
            cus.nameEn AS nameEn,
            cus.nameKh AS nameKh,
            cus.phone AS phone,
            cus.email AS email
        FROM SaleEntity s
        INNER JOIN (
            SELECT _si.saleId AS saleId FROM SaleItemEntity _si
            WHERE (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR _si.productId = :#{#filter.productId})
            GROUP BY _si.saleId
        ) si ON si.saleId = s.id
        LEFT JOIN CustomerEntity cus ON cus.id = s.customerId
        WHERE
            (:startDate IS NULL OR s.date >= :startDate) AND
            (:endDate IS NULL OR s.date <= :endDate) AND
            (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR s.createdBy = :#{#filter.user}) AND
            (:#{#filter.bIds} IS NULL OR s.billerId IN :#{#filter.bIds}) AND
            (:#{#filter.whIds} IS NULL OR s.warehouseId IN :#{#filter.whIds}) AND
            (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR s.id = :#{#filter.id}) AND
            (:#{#filter.referenceNo} IS NULL OR :#{#filter.referenceNo} = '' OR s.referenceNo = :#{#filter.referenceNo}) AND
            (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR s.billerId = :#{#filter.billerId}) AND
            (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR s.warehouseId = :#{#filter.warehouseId}) AND
            (:#{#filter.customerId} IS NULL OR :#{#filter.customerId} = '' OR s.customerId = :#{#filter.customerId}) AND
            (:#{#filter.status} IS NULL OR :#{#filter.status} = '' OR s.status = :#{#filter.status}) AND
            (:#{#filter.paymentStatus} IS NULL OR :#{#filter.paymentStatus} = '' OR s.paymentStatus = :#{#filter.paymentStatus}) AND
            (:#{#filter.deliveryStatus} IS NULL OR :#{#filter.deliveryStatus} = '' OR s.deliveryStatus = :#{#filter.deliveryStatus}) AND
            (:#{#filter.createdBy} IS NULL OR s.createdBy = :#{#filter.createdBy}) AND
            (:#{#filter.updatedBy} IS NULL OR s.updatedBy = :#{#filter.updatedBy})
        GROUP BY s.customerId
    """)
    Page<Tuple> findCustomersSalesReport(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("filter") GeneralSalesReportRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            s.id AS id,
            s.saleOrderId AS saleOrderId,
            s.date AS date,
            s.referenceNo AS referenceNo,
            '' AS soReferenceNo,
            CONCAT(b.companyEn, ' (', b.code, ')') AS biller,
            w.name AS warehouse,
            cus.nameEn AS customer,
            s.total AS total,
            s.shipping AS shipping,
            s.orderDiscount AS orderDiscount,
            s.orderDiscountId AS orderDiscountId,
            s.orderTax AS orderTax,
            t.name AS orderTaxId,
            s.grandTotal AS grandTotal,
            s.paid AS paid,
            s.grandTotal - s.paid AS balance,
            s.changes AS changes,
            ROUND(ci.cost, 2) AS cost,
            s.status AS status,
            s.paymentStatus AS paymentStatus,
            s.deliveryStatus AS deliveryStatus,
            s.note AS note,
            s.attachment AS attachment,
            COALESCE(CONCAT(e.firstName, ' ', e.lastName), u.username) AS createdBy
        FROM SaleEntity s
        LEFT JOIN BillerEntity b ON b.id = s.billerId
        LEFT JOIN WarehouseEntity w ON w.id = s.warehouseId
        LEFT JOIN CustomerEntity cus ON cus.id = s.customerId
        LEFT JOIN TaxRateEntity t ON t.id = s.orderTaxId
        LEFT JOIN UserEntity u ON u.userId = s.createdBy
        LEFT JOIN EmployeeEntity e ON e.id = u.employeeId
        INNER JOIN (
            SELECT _si.saleId AS saleId, SUM(_si.quantity * _si.baseUnitCost) AS cost FROM SaleItemEntity _si
            GROUP BY _si.saleId
        ) ci ON ci.saleId = s.id
        INNER JOIN (
            SELECT _si.saleId AS saleId FROM SaleItemEntity _si
            WHERE (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR _si.productId = :#{#filter.productId})
            GROUP BY _si.saleId
        ) si ON si.saleId = s.id
        WHERE
            (:startDate IS NULL OR s.date >= :startDate) AND
            (:endDate IS NULL OR s.date <= :endDate) AND
            (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR s.createdBy = :#{#filter.user}) AND
            (:#{#filter.bIds} IS NULL OR s.billerId IN :#{#filter.bIds}) AND
            (:#{#filter.whIds} IS NULL OR s.warehouseId IN :#{#filter.whIds}) AND
            (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR s.id = :#{#filter.id}) AND
            (:#{#filter.referenceNo} IS NULL OR :#{#filter.referenceNo} = '' OR s.referenceNo = :#{#filter.referenceNo}) AND
            (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR s.billerId = :#{#filter.billerId}) AND
            (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR s.warehouseId = :#{#filter.warehouseId}) AND
            (:#{#filter.customerId} IS NULL OR :#{#filter.customerId} = '' OR s.customerId = :#{#filter.customerId}) AND
            (:#{#filter.status} IS NULL OR :#{#filter.status} = '' OR s.status = :#{#filter.status}) AND
            (:#{#filter.paymentStatus} IS NULL OR :#{#filter.paymentStatus} = '' OR s.paymentStatus = :#{#filter.paymentStatus}) AND
            (:#{#filter.deliveryStatus} IS NULL OR :#{#filter.deliveryStatus} = '' OR s.deliveryStatus = :#{#filter.deliveryStatus}) AND
            (:#{#filter.createdBy} IS NULL OR s.createdBy = :#{#filter.createdBy}) AND
            (:#{#filter.updatedBy} IS NULL OR s.updatedBy = :#{#filter.updatedBy})
        ORDER BY s.referenceNo, s.date DESC
    """)
    List<Tuple> findSalesDetailReport(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("filter") GeneralSalesReportRequest filter);

    @Query(value = """
        SELECT
            si.saleId AS saleId,
            prd.productId AS productId,
            prd.productCode AS productCode,
            prd.barCode AS barCode,
            prd.productNameEn AS productNameEn,
            prd.productNameKh AS productNameKh,
            b.name AS brand,
            c.name AS category,
            u.unitCode AS unitCode,
            u.unitNameEn AS unitNameEn,
            u.unitNameKh AS unitNameKh,
            si.unitQuantity AS unitQuantity,
            si.unitPrice AS unitPrice,
            si.expiry AS expiry,
            si.discount AS discount,
            si.itemDiscount AS itemDiscount,
            t.name AS taxRate,
            si.itemTax AS itemTax,
            si.subtotal AS subtotal,
            si.description AS description
        FROM SaleEntity s
        INNER JOIN SaleItemEntity si ON si.saleId = s.id
        LEFT JOIN ProductEntity prd ON prd.productId = si.productId
        LEFT JOIN BrandEntity b ON b.brandId = prd.brandId
        LEFT JOIN CategoryEntity c ON c.categoryId = prd.categoryId
        LEFT JOIN UnitEntity u ON u.unitId = si.unitId
        LEFT JOIN TaxRateEntity t ON t.id = si.taxRateId
        WHERE s.id = :id
    """)
    List<Tuple> findSaleItemsDetailReport(Long id);

    @Query(value = "SELECT " +
            "p.productId AS productId, " +
            "p.productCode AS productCode, " +
            "p.productNameEn AS productName, " +
            "c.name AS category, " +
            "b.name AS brand, " +
            "u.unitCode AS unitCode, " +
            "u.unitNameEn AS unitNameEn, " +
            "u.unitNameKh AS unitNameKh, " +
            "SUM(si.quantity) AS quantitySold, " +
            "ROUND(SUM(si.subtotal), 2) AS totalPriceAmount, " +
            "ROUND(SUM(si.quantity * si.baseUnitCost), 2) AS totalCostAmount, " +
            "ROUND(SUM(si.subtotal - (si.quantity * si.baseUnitCost)), 2) AS totalProfit " +
            "FROM SaleItemEntity si " +
            "JOIN SaleEntity s ON si.saleId = s.id " +
            "LEFT JOIN BillerEntity bil ON bil.id = s.billerId " +
            "LEFT JOIN WarehouseEntity w ON w.id = s.warehouseId " +
            "LEFT JOIN UserEntity usr ON usr.userId = s.createdBy " +
            "LEFT JOIN EmployeeEntity e ON e.id = usr.employeeId " +
            "JOIN ProductEntity p ON p.productId = si.productId " +
            "LEFT JOIN (  " +
            "    SELECT  " +
            "    _u.unitId AS unitId, _u.unitCode AS unitCode, _u.unitNameEn AS unitNameEn, _u.unitNameKh AS unitNameKh, _pu.id.productId AS productId " +
            "    FROM UnitEntity _u  " +
            "    INNER JOIN ProductUnitEntity _pu ON _pu.id.unitId = _u.unitId AND _u.punitId IS NULL " +
            ") u ON u.productId = p.productId  " +
            "JOIN p.category c " +
            "JOIN p.brand b " +
            "WHERE s.date BETWEEN :startDate AND :endDate " +
            "AND s.status IN ('partial', 'completed') " +
            "AND (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR s.createdBy = :#{#filter.user}) " +
            "AND (:#{#filter.bIds} IS NULL OR s.billerId IN :#{#filter.bIds}) " +
            "AND (:#{#filter.whIds} IS NULL OR s.warehouseId IN :#{#filter.whIds}) " +
            "AND (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR s.billerId = :#{#filter.billerId}) " +
            "AND (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR s.warehouseId = :#{#filter.warehouseId}) " +
            "AND p.status = 1 " +
            "AND (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR p.productId = :#{#filter.productId}) " +
            "AND (:#{#filter.productCode} IS NULL OR :#{#filter.productCode} = '' OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :#{#filter.productCode}, '%'))) " +
            "AND (:#{#filter.barCode} IS NULL OR :#{#filter.barCode} = '' OR LOWER(p.barCode) LIKE LOWER(CONCAT('%', :#{#filter.barCode}, '%'))) " +
            "AND (:#{#filter.productNameEn} IS NULL OR :#{#filter.productNameEn} = '' OR LOWER(p.productNameEn) LIKE LOWER(CONCAT('%', :#{#filter.productNameEn}, '%'))) " +
            "AND (:#{#filter.productNameKh} IS NULL OR :#{#filter.productNameKh} = '' OR LOWER(p.productNameKh) LIKE LOWER(CONCAT('%', :#{#filter.productNameKh}, '%'))) " +
            "AND (:#{#filter.brandId} IS NULL OR :#{#filter.brandId} = '' OR p.brandId = :#{#filter.brandId}) " +
            "AND (:#{#filter.categoryId} IS NULL OR :#{#filter.categoryId} = '' OR p.categoryId = :#{#filter.categoryId}) " +
            "AND (:#{#filter.type} IS NULL OR :#{#filter.type} = '' OR p.type = :#{#filter.type}) " +
            "AND (:#{#filter.createdBy} IS NULL OR s.createdBy = :#{#filter.createdBy}) " +
            "AND (:#{#filter.updatedBy} IS NULL OR s.updatedBy = :#{#filter.updatedBy}) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(p.productCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(p.productNameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(p.productNameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(c.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(b.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(u.unitCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(u.unitNameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(u.unitNameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(bil.companyEn, ' (', bil.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(w.name, ' (', w.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CASE WHEN e.id IS NULL THEN usr.username ELSE CONCAT(COALESCE(e.firstName, ''), ' ', COALESCE(e.lastName, '')) END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    ) " +
            "GROUP BY p.productId")
    Page<Tuple> findTopSellingProducts(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("filter") TopSellingProductReportRequest filter, Pageable pageable);

    @Query("SELECT " +
            "pr.date AS openedAt, pr.id AS id, pr.userId AS userId, pr.status AS status, " +
            "pr.cashInHand AS cashInHand, " +
            "CASE WHEN pr.status = 'close' THEN pr.totalCash ELSE NULL END AS totalCash, " +
            "CASE WHEN pr.status = 'close' THEN pr.totalCheques ELSE NULL END AS totalCheques, " +
            "CASE WHEN pr.status = 'close' THEN pr.totalCcSlips ELSE NULL END AS totalCcSlips, " +
            "CASE WHEN pr.status = 'close' THEN pr.totalCashSubmitted ELSE NULL END AS totalCashSubmitted, " +
            "CASE WHEN pr.status = 'close' THEN pr.totalChequesSubmitted ELSE NULL END AS totalChequesSubmitted, " +
            "CASE WHEN pr.status = 'close' THEN pr.totalCcSlipsSubmitted ELSE NULL END AS totalCcSlipsSubmitted, " +
            "pr.transferOpenedBills AS transferOpenedBills, " +
            "pr.note AS note, " +
            "pr.closedAt AS closedAt, " +
            "CASE WHEN u.employeeId IS NOT NULL THEN CONCAT(e.firstName, ' ', e.lastName) ELSE u.username END AS name " +
            "FROM PosRegisterEntity pr " +
            "LEFT JOIN UserEntity u ON pr.userId = u.userId " +
            "LEFT JOIN EmployeeEntity e ON u.employeeId = e.id " +
            "WHERE (:user IS NULL OR pr.userId = :user) " +
            "AND (:id IS NULL OR pr.id = :id) " +
            "AND (:userId IS NULL OR pr.userId = :userId) " +
            "AND (:status IS NULL OR pr.status = :status) " +
            "AND (:name IS NULL OR " +
            "(u.username LIKE %:name% OR CONCAT(e.firstName, ' ', e.lastName) LIKE %:name%))"+
            "AND (:startDate IS NULL OR pr.date >= :startDate) " +
            "AND (:endDate IS NULL OR pr.date <= :endDate) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(FUNCTION('DATE_FORMAT', pr.date, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(FUNCTION('DATE_FORMAT', pr.closedAt, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(pr.status) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(pr.note) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CASE WHEN e.id IS NULL THEN u.username ELSE CONCAT(COALESCE(e.firstName, ''), ' ', COALESCE(e.lastName, '')) END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    ) ")
    Page<Tuple> findRegisterReports(@Param("user") Long user, @Param("id") Long id, @Param("userId") Long userId, @Param("status") String status, @Param("name") String name, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("filter") RegisterReportRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            reg.id AS id,
            reg.date AS openedAt,
            reg.closedAt AS closedAt,
            reg.userId AS userId,
            CASE WHEN u.employeeId IS NOT NULL THEN CONCAT(e.firstName, ' ', e.lastName) ELSE u.username END AS name,
            reg.cashInHand AS cashInHand,
            reg.totalCash AS totalCash,
            reg.totalCheques AS totalCheques,
            reg.totalCcSlips AS totalCcSlips,
            reg.totalCashSubmitted AS totalCashSubmitted,
            reg.totalChequesSubmitted AS totalChequesSubmitted,
            reg.totalCcSlipsSubmitted AS totalCcSlipsSubmitted,
            reg.transferOpenedBills AS transferOpenedBills,
            reg.note AS note,
            reg.status AS status
        FROM PosRegisterEntity reg
        LEFT JOIN UserEntity u ON reg.userId = u.userId
        LEFT JOIN EmployeeEntity e ON u.employeeId = e.id
        WHERE
            (:#{#filter.id} IS NULL OR reg.id = :#{#filter.id}) AND
            (:#{#filter.userId} IS NULL OR (reg.userId = :#{#filter.userId} AND reg.status = 'open'))
    """)
    Tuple findRegisterDetails(@Param("filter") RegisterReportRequest filter);

    @Query(value = """
        SELECT
            pm.id AS id,
            pm.name AS name,
            pm.type AS type,
            SUM(p.amount) AS amount
        FROM PaymentEntity p
        INNER JOIN PosRegisterEntity reg ON reg.id = :id
        INNER JOIN PaymentMethodEntity pm ON pm.id = p.paymentMethodId
        INNER JOIN SaleEntity s ON s.id = p.saleId AND s.createdBy = reg.userId AND (s.date BETWEEN reg.date AND COALESCE(reg.closedAt, CURRENT_TIMESTAMP))
        WHERE s.pos = 1
        GROUP BY pm.id
    """)
    List<Tuple> findRegisterPayments(Long id);

    @Query(value = """
        SELECT
            c.id AS id,
            c.companyEn AS companyEn,
            c.companyKh AS companyKh,
            c.nameEn AS nameEn,
            c.nameKh AS nameKh,
            c.phone AS phone,
            c.email AS email,
            COALESCE(SUM(s.totalItems), 0) AS totalItems,
            COALESCE(SUM(s.grandTotal), 0) AS amount
        FROM PosRegisterEntity reg
        INNER JOIN SaleEntity s ON s.createdBy = reg.userId AND (s.date BETWEEN reg.date AND COALESCE(reg.closedAt, CURRENT_TIMESTAMP))
        INNER JOIN CustomerEntity c ON c.id = s.customerId
        WHERE reg.id = :id
        GROUP BY c.id
    """)
    List<Tuple> findRegisterCustomers(Long id);

    @Query(value = """
        SELECT
            COALESCE(s.orderDiscount, 0) AS orderDiscount,
            COALESCE(s.orderTax, 0) AS orderTax,
            COALESCE(s.shipping, 0) AS shipping,
            COALESCE(s.grandTotal, 0) AS grandTotal,
            0 AS refund,
            COALESCE(e.expense, 0) AS expense,
            COALESCE(s.totalTransactions, 0) AS totalTransactions
        FROM PosRegisterEntity reg
        LEFT JOIN (
            SELECT
                _reg.id AS regId,
                COALESCE(SUM(_s.orderDiscount), 0) AS orderDiscount,
                COALESCE(SUM(_s.orderTax), 0) AS orderTax,
                COALESCE(SUM(_s.shipping), 0) AS shipping,
                COALESCE(SUM(_s.grandTotal), 0) AS grandTotal,
                COALESCE(COUNT(_s.id), 0) AS totalTransactions
            FROM PosRegisterEntity _reg
            INNER JOIN SaleEntity _s ON _s.createdBy = _reg.userId AND (_s.date BETWEEN _reg.date AND COALESCE(_reg.closedAt, CURRENT_TIMESTAMP))
            WHERE _reg.id = :id
        ) s ON s.regId = reg.id
        LEFT JOIN (
            SELECT
                _reg.id AS regId,
                COALESCE(SUM(_e.amount), 0) AS expense
            FROM PosRegisterEntity _reg
            INNER JOIN ExpenseEntity _e ON _e.createdBy = _reg.userId AND (_e.date BETWEEN _reg.date AND COALESCE(_reg.closedAt, CURRENT_TIMESTAMP))
            WHERE _reg.id = :id
        ) e ON e.regId = reg.id
        WHERE reg.id = :id
    """)
    Tuple findRegisterSaleDetails(Long id);

    @Query(value = """
        SELECT
            c.categoryId AS categoryId,
            c.code AS code,
            c.name AS name
        FROM PosRegisterEntity reg
        INNER JOIN SaleEntity s ON s.createdBy = reg.userId AND (s.date BETWEEN reg.date AND COALESCE(reg.closedAt, CURRENT_TIMESTAMP))
        INNER JOIN SaleItemEntity si ON si.saleId = s.id
        INNER JOIN ProductEntity p ON p.productId = si.productId
        INNER JOIN CategoryEntity c ON c.categoryId = p.categoryId
        WHERE reg.id = :id
        GROUP BY c.categoryId
    """)
    List<Tuple> findRegisterSaleCategories(Long id);

    @Query(value = """
        SELECT
            p.productId AS productId,
            p.productCode AS productCode,
            p.barCode AS barCode,
            p.productNameEn AS productNameEn,
            p.productNameKh AS productNameKh,
            b.name AS brand,
            c.name AS category,
            COALESCE(SUM(si.unitQuantity), 0) AS unitQuantity,
            ROUND(COALESCE(SUM(si.unitQuantity * si.itemDiscount), 0), 2) AS discount,
            ROUND(COALESCE(SUM(si.unitQuantity * si.itemTax), 0), 2) AS tax,
            ROUND(COALESCE(SUM(si.subtotal), 0), 2) AS subtotal
        FROM PosRegisterEntity reg
        INNER JOIN SaleEntity s ON s.createdBy = reg.userId AND (s.date BETWEEN reg.date AND COALESCE(reg.closedAt, CURRENT_TIMESTAMP))
        INNER JOIN SaleItemEntity si ON si.saleId = s.id
        INNER JOIN ProductEntity p ON p.productId = si.productId
        INNER JOIN CategoryEntity c ON c.categoryId = p.categoryId
        LEFT JOIN BrandEntity b ON b.id = p.brandId
        WHERE reg.id = :id AND c.categoryId = :categoryId
        GROUP BY p.productId
    """)
    List<Tuple> findRegisterSaleItems(Long id, Long categoryId);
}
