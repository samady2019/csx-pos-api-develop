package kh.com.csx.posapi.repository.report;

import jakarta.persistence.Tuple;
import kh.com.csx.posapi.dto.report.financialReport.*;
import kh.com.csx.posapi.entity.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FinancialReportRepository extends JpaRepository<PaymentEntity, Long> {
    @Query(value = """
        SELECT
            xp.id AS id,
            xp.date AS date,
            xp.referenceNo AS referenceNo,
            CONCAT(b.companyEn, ' (', b.code, ')') AS biller,
            c.name AS expenseCategory,
            COALESCE(xp.amount, 0) AS amount,
            COALESCE(0, 0) AS paid,
            COALESCE(xp.amount, 0) - COALESCE(0, 0) AS balance,
            xp.attachment AS attachment,
            xp.note AS note,
            xp.paymentStatus AS paymentStatus,
            COALESCE(CONCAT(e.firstName, ' ', e.lastName), u.username) AS createdBy
        FROM ExpenseEntity xp
        LEFT JOIN BillerEntity b ON b.id = xp.billerId
        LEFT JOIN ExpenseCategoryEntity c ON c.id = xp.expenseCategoryId
        LEFT JOIN UserEntity u ON u.userId = xp.createdBy
        LEFT JOIN EmployeeEntity e ON e.id = u.employeeId
        WHERE
            (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR xp.createdBy = :#{#filter.user}) AND
            (:#{#filter.bIds} IS NULL OR xp.billerId IN :#{#filter.bIds}) AND
            (:#{#filter.id} IS NULL OR xp.id = :#{#filter.id}) AND
            (:#{#filter.expenseCategoryId} IS NULL OR xp.expenseCategoryId = :#{#filter.expenseCategoryId}) AND
            (:#{#filter.referenceNo} IS NULL OR LOWER(xp.referenceNo) LIKE LOWER(CONCAT('%', :#{#filter.referenceNo}, '%'))) AND
            (:#{#filter.billerId} IS NULL OR xp.billerId = :#{#filter.billerId}) AND
            (:#{#filter.paymentStatus} IS NULL OR :#{#filter.paymentStatus} = '' OR xp.paymentStatus = :#{#filter.paymentStatus}) AND
            (:#{#filter.createdBy} IS NULL OR xp.createdBy = :#{#filter.createdBy}) AND
            (:#{#filter.updatedBy} IS NULL OR xp.updatedBy = :#{#filter.updatedBy}) AND
            (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (xp.date BETWEEN :#{#filter.start} AND :#{#filter.end})) AND
            (
                (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') OR
                (
                    LOWER(FUNCTION('DATE_FORMAT', xp.date, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(xp.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CONCAT(b.companyEn, ' (', b.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(c.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(xp.paymentStatus) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(xp.note) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CASE WHEN e.id IS NULL THEN u.username ELSE CONCAT(COALESCE(e.firstName, ''), ' ', COALESCE(e.lastName, '')) END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                )
            )
    """)
    Page<Tuple> findExpenses(@Param("filter") ExpenseRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            xpc.id AS id,
            xpc.code AS code,
            xpc.name AS name,
            COALESCE(:#{#filter.year}, '') AS year,
            COALESCE(xp.amount, 0) AS total
        FROM ExpenseCategoryEntity xpc
        LEFT JOIN (
            SELECT
                _xp.expenseCategoryId AS expenseCategoryId,
                COALESCE(SUM(_xp.amount), 0) AS amount,
                COALESCE(:#{#filter.year}, '') AS year
            FROM ExpenseEntity _xp
            WHERE
                (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR _xp.createdBy = :#{#filter.user}) AND
                (:#{#filter.bIds} IS NULL OR _xp.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.billerId} IS NULL OR _xp.billerId = :#{#filter.billerId}) AND
                (:#{#filter.createdBy} IS NULL OR _xp.createdBy = :#{#filter.createdBy}) AND
                (:#{#filter.updatedBy} IS NULL OR _xp.updatedBy = :#{#filter.updatedBy}) AND
                (YEAR(_xp.date) = :#{#filter.year})
            GROUP BY _xp.expenseCategoryId, YEAR(_xp.date)
        ) xp ON xp.expenseCategoryId = xpc.id
        WHERE
            (:#{#filter.id} IS NULL OR xpc.id = :#{#filter.id}) AND
            (:#{#filter.expenseCategoryId} IS NULL OR xpc.id = :#{#filter.expenseCategoryId}) AND
            (:#{#filter.code} IS NULL OR :#{#filter.code} = '' OR LOWER(xpc.code) LIKE LOWER(CONCAT('%', :#{#filter.code}, '%'))) AND
            (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR LOWER(xpc.name) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%'))) AND
            (
                (:#{#filter.all} IS NOT NULL AND :#{#filter.all} = 1) OR
                (
                    (:#{#filter.all} IS NULL OR :#{#filter.all} != 1) AND
                    (xp.amount IS NOT NULL AND xp.amount != 0)
                )
            )
    """)
    Page<Tuple> findExpensesMonthly(@Param("filter") ExpenseMonthlyRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            xpc.id AS id,
            COALESCE(:#{#filter.month}, '') AS month,
            COALESCE(xp.amount, 0) AS amount
        FROM ExpenseCategoryEntity xpc
        LEFT JOIN (
            SELECT
                _xp.expenseCategoryId AS expenseCategoryId,
                COALESCE(SUM(_xp.amount), 0) AS amount
            FROM ExpenseEntity _xp
            WHERE
                (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR _xp.createdBy = :#{#filter.user}) AND
                (:#{#filter.bIds} IS NULL OR _xp.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.billerId} IS NULL OR _xp.billerId = :#{#filter.billerId}) AND
                (:#{#filter.expenseCategoryId} IS NULL OR _xp.expenseCategoryId = :#{#filter.expenseCategoryId}) AND
                (:#{#filter.createdBy} IS NULL OR _xp.createdBy = :#{#filter.createdBy}) AND
                (:#{#filter.updatedBy} IS NULL OR _xp.updatedBy = :#{#filter.updatedBy}) AND
                (YEAR(_xp.date) = :#{#filter.year} AND MONTH(_xp.date) = :#{#filter.month})
            GROUP BY _xp.expenseCategoryId
        ) xp ON xp.expenseCategoryId = xpc.id
        WHERE (:#{#filter.expenseCategoryId} IS NULL OR xpc.id = :#{#filter.expenseCategoryId})
    """)
    Tuple findFirstExpenseByMonth(@Param("filter") ExpenseMonthlyRequest filter);

    @Query(value = """
        SELECT
            p.id AS id,
            p.date AS date,
            p.dueDate AS dueDate,
            p.referenceNo AS referenceNo,
            CONCAT(b.companyEn, ' (', b.code, ')') AS biller,
            w.name AS warehouse,
            s.companyEn AS supplier,
            NULL AS customer,
            COALESCE(p.grandTotal, 0) AS grandTotal,
            COALESCE(p.paid, 0) AS paid,
            COALESCE(p.grandTotal, 0) - COALESCE(p.paid, 0) AS balance,
            p.status AS status,
            p.paymentStatus AS paymentStatus,
            p.attachment AS attachment,
            p.note AS note,
            COALESCE(CONCAT(e.firstName, ' ', e.lastName), u.username) AS createdBy
        FROM PurchaseEntity p
        LEFT JOIN BillerEntity b ON b.id = p.billerId
        LEFT JOIN WarehouseEntity w ON w.id = p.warehouseId
        LEFT JOIN SupplierEntity s ON s.id = p.supplierId
        LEFT JOIN UserEntity u ON u.userId = p.createdBy
        LEFT JOIN EmployeeEntity e ON e.id = u.employeeId
        INNER JOIN SettingEntity set ON 1=1
        WHERE
            p.status != 'pending' AND p.paymentStatus != 'paid' AND
            p.dueDate IS NOT NULL AND CURRENT_DATE >= DATEADD(DAY, -COALESCE(set.alertDay, 0), p.dueDate) AND
            (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR p.createdBy = :#{#filter.user}) AND
            (:#{#filter.bIds} IS NULL OR p.billerId IN :#{#filter.bIds}) AND
            (:#{#filter.whIds} IS NULL OR p.warehouseId IN :#{#filter.whIds}) AND
            (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR p.id = :#{#filter.id}) AND
            (:#{#filter.referenceNo} IS NULL OR :#{#filter.referenceNo} = '' OR p.referenceNo = :#{#filter.referenceNo}) AND
            (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR p.billerId = :#{#filter.billerId}) AND
            (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR p.warehouseId = :#{#filter.warehouseId}) AND
            (:#{#filter.supplierId} IS NULL OR :#{#filter.supplierId} = '' OR p.supplierId = :#{#filter.supplierId}) AND
            (:#{#filter.status} IS NULL OR :#{#filter.status} = '' OR p.status = :#{#filter.status}) AND
            (:#{#filter.paymentStatus} IS NULL OR :#{#filter.paymentStatus} = '' OR p.paymentStatus = :#{#filter.paymentStatus}) AND
            (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR p.createdBy = :#{#filter.createdBy}) AND
            (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR p.updatedBy = :#{#filter.updatedBy}) AND
            (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (p.date BETWEEN :#{#filter.start} AND :#{#filter.end})) AND
            (
                (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') OR
                (
                    LOWER(FUNCTION('DATE_FORMAT', p.date, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(FUNCTION('DATE_FORMAT', p.dueDate, dFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(p.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CONCAT(b.companyEn, ' (', b.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CONCAT(w.name, ' (', w.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(s.companyEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(s.nameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(p.status) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(p.paymentStatus) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(p.note) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CASE WHEN e.id IS NULL THEN u.username ELSE CONCAT(COALESCE(e.firstName, ''), ' ', COALESCE(e.lastName, '')) END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                )
            )
    """)
    Page<Tuple> findPurchasePaymentAlerts(@Param("filter") PaymentRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            pay.id AS id,
            pay.purchaseOrderId AS purchaseOrderId,
            pay.purchaseId AS purchaseId,
            pay.saleOrderId AS saleOrderId,
            pay.saleId AS saleId,
            pay.expenseId AS expenseId,
            pay.date AS date,
            pay.referenceNo AS referenceNo,
            po.referenceNo AS poRefNo,
            p.referenceNo AS puRefNo,
            NULL AS soRefNo,
            s.referenceNo AS slRefNo,
            xp.referenceNo AS expRefNo,
            CONCAT(b.companyEn, ' (', b.code, ')') AS biller,
            spp.companyEn AS supplier,
            cus.nameEn AS customer,
            pm.name AS paymentMethod,
            pay.amount AS amount,
            pay.attachment AS attachment,
            pay.note AS note,
            pay.type AS type,
            COALESCE(CONCAT(e.firstName, ' ', e.lastName), u.username) AS createdBy
        FROM PaymentEntity pay
        LEFT JOIN PurchaseOrderEntity po ON po.id = pay.purchaseOrderId
        LEFT JOIN PurchaseEntity p ON p.id = pay.purchaseId
        LEFT JOIN SaleEntity s ON s.id = pay.saleId
        LEFT JOIN ExpenseEntity xp ON xp.id = pay.expenseId
        LEFT JOIN BillerEntity b ON b.id = pay.billerId
        LEFT JOIN SupplierEntity spp ON (spp.id = po.supplierId OR spp.id = p.supplierId)
        LEFT JOIN CustomerEntity cus ON cus.id = s.customerId
        LEFT JOIN PaymentMethodEntity pm ON pm.id = pay.paymentMethodId
        LEFT JOIN UserEntity u ON u.userId = pay.createdBy
        LEFT JOIN EmployeeEntity e ON e.id = u.employeeId
        WHERE
            (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR pay.createdBy = :#{#filter.user}) AND
            (:#{#filter.bIds} IS NULL OR pay.billerId IN :#{#filter.bIds}) AND
            (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR pay.id = :#{#filter.id}) AND
            (:#{#filter.referenceNo} IS NULL OR :#{#filter.referenceNo} = '' OR LOWER(pay.referenceNo) LIKE CONCAT('%', :#{#filter.referenceNo}, '%')) AND
            (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR pay.billerId = :#{#filter.billerId}) AND
            (:#{#filter.purchaseOrderId} IS NULL OR :#{#filter.purchaseOrderId} = '' OR pay.purchaseOrderId = :#{#filter.purchaseOrderId}) AND
            (:#{#filter.purchaseId} IS NULL OR :#{#filter.purchaseId} = '' OR pay.purchaseId = :#{#filter.purchaseId}) AND
            (:#{#filter.saleOrderId} IS NULL OR :#{#filter.saleOrderId} = '' OR pay.saleOrderId = :#{#filter.saleOrderId}) AND
            (:#{#filter.saleId} IS NULL OR :#{#filter.saleId} = '' OR pay.saleId = :#{#filter.saleId}) AND
            (:#{#filter.expenseId} IS NULL OR :#{#filter.expenseId} = '' OR pay.expenseId = :#{#filter.expenseId}) AND
            (:#{#filter.paymentMethodId} IS NULL OR :#{#filter.paymentMethodId} = '' OR pay.paymentMethodId = :#{#filter.paymentMethodId}) AND
            (:#{#filter.type} IS NULL OR :#{#filter.type} = '' OR pay.type = :#{#filter.type}) AND
            (:#{#filter.tranPurchaseOrder} IS NULL OR :#{#filter.tranPurchaseOrder} = '' OR (pay.purchaseOrderId IS NOT NULL)) AND
            (:#{#filter.tranPurchase} IS NULL OR :#{#filter.tranPurchase} = '' OR (pay.purchaseId IS NOT NULL)) AND
            (:#{#filter.tranSaleOrder} IS NULL OR :#{#filter.tranSaleOrder} = '' OR (pay.saleOrderId IS NOT NULL)) AND
            (:#{#filter.tranSale} IS NULL OR :#{#filter.tranSale} = '' OR (pay.saleId IS NOT NULL)) AND
            (:#{#filter.tranExpense} IS NULL OR :#{#filter.tranExpense} = '' OR (pay.expenseId IS NOT NULL)) AND
            (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR pay.createdBy = :#{#filter.createdBy}) AND
            (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR pay.updatedBy = :#{#filter.updatedBy}) AND
            (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (pay.date BETWEEN :#{#filter.start} AND :#{#filter.end})) AND
            (
                (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') OR
                (
                    LOWER(FUNCTION('DATE_FORMAT', pay.date, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(pay.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(po.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(p.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(s.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(xp.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CONCAT(b.companyEn, ' (', b.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(spp.companyEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(spp.nameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(cus.companyEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(cus.nameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(pm.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(pay.type) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(pay.note) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CASE WHEN e.id IS NULL THEN u.username ELSE CONCAT(COALESCE(e.firstName, ''), ' ', COALESCE(e.lastName, '')) END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                )
            )
    """)
    Page<Tuple> findPayments(@Param("filter") PaymentRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            pm.id AS id,
            pm.name AS name,
            pm.type AS type,
            COALESCE(beginning.amount, 0) AS beginning,
            COALESCE(purchaseOrder.amount, 0) AS purchaseOrder,
            COALESCE(purchase.amount, 0) AS purchase,
            COALESCE(0, 0) AS saleOrder,
            COALESCE(sale.amount, 0) AS sale,
            COALESCE(expense.amount, 0) AS expense,
            (
                COALESCE(beginning.amount, 0) +
                COALESCE(sale.amount, 0) -
                COALESCE(purchaseOrder.amount, 0) -
                COALESCE(purchase.amount, 0) -
                COALESCE(expense.amount, 0)
            ) AS balance
        FROM PaymentMethodEntity pm
        LEFT JOIN (
            SELECT
                _pm.id AS paymentMethodId,
                (
                    COALESCE(sale.amount, 0) -
                    COALESCE(purchaseOrder.amount, 0) -
                    COALESCE(purchase.amount, 0) -
                    COALESCE(expense.amount, 0)
                ) AS amount
            FROM PaymentMethodEntity _pm
            LEFT JOIN (
                SELECT
                    pay.paymentMethodId AS paymentMethodId,
                    COALESCE(SUM(pay.amount), 0) AS amount
                FROM PaymentEntity pay
                INNER JOIN PurchaseOrderEntity po ON po.id = pay.purchaseOrderId
                WHERE
                    pay.purchaseOrderId IS NOT NULL AND
                    (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR pay.createdBy = :#{#filter.user}) AND
                    (:#{#filter.bIds} IS NULL OR pay.billerId IN :#{#filter.bIds}) AND
                    (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR pay.billerId = :#{#filter.billerId}) AND
                    (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR pay.createdBy = :#{#filter.createdBy}) AND
                    (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR pay.updatedBy = :#{#filter.updatedBy}) AND
                    pay.date < :#{#filter.start}
                GROUP BY pay.paymentMethodId
            ) purchaseOrder ON purchaseOrder.paymentMethodId = _pm.id
            LEFT JOIN (
                SELECT
                    pay.paymentMethodId AS paymentMethodId,
                    COALESCE(SUM(pay.amount), 0) AS amount
                FROM PaymentEntity pay
                INNER JOIN PurchaseEntity p ON p.id = pay.purchaseId
                WHERE
                    pay.purchaseId IS NOT NULL AND
                    (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR pay.createdBy = :#{#filter.user}) AND
                    (:#{#filter.bIds} IS NULL OR pay.billerId IN :#{#filter.bIds}) AND
                    (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR pay.billerId = :#{#filter.billerId}) AND
                    (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR pay.createdBy = :#{#filter.createdBy}) AND
                    (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR pay.updatedBy = :#{#filter.updatedBy}) AND
                    pay.date < :#{#filter.start}
                GROUP BY pay.paymentMethodId
            ) purchase ON purchase.paymentMethodId = _pm.id
            LEFT JOIN (
                SELECT
                    pay.paymentMethodId AS paymentMethodId,
                    COALESCE(SUM(pay.amount), 0) AS amount
                FROM PaymentEntity pay
                INNER JOIN SaleEntity s ON s.id = pay.saleId
                WHERE
                    pay.saleId IS NOT NULL AND
                    (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR pay.createdBy = :#{#filter.user}) AND
                    (:#{#filter.bIds} IS NULL OR pay.billerId IN :#{#filter.bIds}) AND
                    (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR pay.billerId = :#{#filter.billerId}) AND
                    (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR pay.createdBy = :#{#filter.createdBy}) AND
                    (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR pay.updatedBy = :#{#filter.updatedBy}) AND
                    pay.date < :#{#filter.start}
                GROUP BY pay.paymentMethodId
            ) sale ON sale.paymentMethodId = _pm.id
            LEFT JOIN (
                SELECT
                    pay.paymentMethodId AS paymentMethodId,
                    COALESCE(SUM(pay.amount), 0) AS amount
                FROM PaymentEntity pay
                INNER JOIN ExpenseEntity xp ON xp.id = pay.expenseId
                WHERE
                    pay.expenseId IS NOT NULL AND
                    (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR pay.createdBy = :#{#filter.user}) AND
                    (:#{#filter.bIds} IS NULL OR pay.billerId IN :#{#filter.bIds}) AND
                    (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR pay.billerId = :#{#filter.billerId}) AND
                    (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR pay.createdBy = :#{#filter.createdBy}) AND
                    (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR pay.updatedBy = :#{#filter.updatedBy}) AND
                    pay.date < :#{#filter.start}
                GROUP BY pay.paymentMethodId
            ) expense ON expense.paymentMethodId = _pm.id
        ) beginning ON beginning.paymentMethodId = pm.id
        LEFT JOIN (
            SELECT
                pay.paymentMethodId AS paymentMethodId,
                COALESCE(SUM(pay.amount), 0) AS amount
            FROM PaymentEntity pay
            INNER JOIN PurchaseOrderEntity po ON po.id = pay.purchaseOrderId
            WHERE
                pay.purchaseOrderId IS NOT NULL AND
                (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR pay.createdBy = :#{#filter.user}) AND
                (:#{#filter.bIds} IS NULL OR pay.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR pay.billerId = :#{#filter.billerId}) AND
                (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR pay.createdBy = :#{#filter.createdBy}) AND
                (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR pay.updatedBy = :#{#filter.updatedBy}) AND
                (pay.date BETWEEN :#{#filter.start} AND :#{#filter.end})
            GROUP BY pay.paymentMethodId
        ) purchaseOrder ON purchaseOrder.paymentMethodId = pm.id
        LEFT JOIN (
            SELECT
                pay.paymentMethodId AS paymentMethodId,
                COALESCE(SUM(pay.amount), 0) AS amount
            FROM PaymentEntity pay
            INNER JOIN PurchaseEntity p ON p.id = pay.purchaseId
            WHERE
                pay.purchaseId IS NOT NULL AND
                (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR pay.createdBy = :#{#filter.user}) AND
                (:#{#filter.bIds} IS NULL OR pay.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR pay.billerId = :#{#filter.billerId}) AND
                (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR pay.createdBy = :#{#filter.createdBy}) AND
                (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR pay.updatedBy = :#{#filter.updatedBy}) AND
                (pay.date BETWEEN :#{#filter.start} AND :#{#filter.end})
            GROUP BY pay.paymentMethodId
        ) purchase ON purchase.paymentMethodId = pm.id
        LEFT JOIN (
            SELECT
                pay.paymentMethodId AS paymentMethodId,
                COALESCE(SUM(pay.amount), 0) AS amount
            FROM PaymentEntity pay
            INNER JOIN SaleEntity s ON s.id = pay.saleId
            WHERE
                pay.saleId IS NOT NULL AND
                (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR pay.createdBy = :#{#filter.user}) AND
                (:#{#filter.bIds} IS NULL OR pay.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR pay.billerId = :#{#filter.billerId}) AND
                (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR pay.createdBy = :#{#filter.createdBy}) AND
                (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR pay.updatedBy = :#{#filter.updatedBy}) AND
                (pay.date BETWEEN :#{#filter.start} AND :#{#filter.end})
            GROUP BY pay.paymentMethodId
        ) sale ON sale.paymentMethodId = pm.id
        LEFT JOIN (
            SELECT
                pay.paymentMethodId AS paymentMethodId,
                COALESCE(SUM(pay.amount), 0) AS amount
            FROM PaymentEntity pay
            INNER JOIN ExpenseEntity xp ON xp.id = pay.expenseId
            WHERE
                pay.expenseId IS NOT NULL AND
                (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR pay.createdBy = :#{#filter.user}) AND
                (:#{#filter.bIds} IS NULL OR pay.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR pay.billerId = :#{#filter.billerId}) AND
                (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR pay.createdBy = :#{#filter.createdBy}) AND
                (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR pay.updatedBy = :#{#filter.updatedBy}) AND
                (pay.date BETWEEN :#{#filter.start} AND :#{#filter.end})
            GROUP BY pay.paymentMethodId
        ) expense ON expense.paymentMethodId = pm.id
        WHERE
            pm.status = 1 AND
            (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR pm.id = :#{#filter.id}) AND
            (:#{#filter.paymentMethodId} IS NULL OR :#{#filter.paymentMethodId} = '' OR pm.id = :#{#filter.paymentMethodId}) AND
            (:#{#filter.paymentMethodName} IS NULL OR :#{#filter.paymentMethodName} = '' OR LOWER(pm.name) LIKE LOWER(CONCAT('%', :#{#filter.paymentMethodName}, '%'))) AND
            (:#{#filter.type} IS NULL OR :#{#filter.type} = '' OR LOWER(pm.type) = LOWER(:#{#filter.type})) AND
            (
                (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') OR
                (
                    LOWER(pm.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(pm.type) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                )
            )
    """)
    Page<Tuple> findCashManagement(@Param("filter") PaymentRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            COALESCE(beginning.totalProfit, 0) AS beginning,
            COALESCE(sale.total, 0) AS totalNetSale,
            ROUND(COALESCE(sale.cost, 0), 2) AS totalCostOfGood,
            COALESCE(purchase.total, 0) AS totalPurchase,
            COALESCE(expense.total, 0) AS totalExpense,
            (
                COALESCE(sale.total, 0) -
                ROUND(COALESCE(sale.cost, 0), 2) -
                COALESCE(expense.total, 0)
            ) AS totalProfit,
            (
                COALESCE(beginning.totalProfit, 0) +
                COALESCE(sale.total, 0) -
                ROUND(COALESCE(sale.cost, 0), 2) -
                COALESCE(expense.total, 0)
            ) AS ending
        FROM (
            SELECT
                COALESCE(sale.total, 0) AS totalNetSale,
                ROUND(COALESCE(sale.cost, 0), 2) AS totalCostOfGood,
                COALESCE(purchase.total, 0) AS totalPurchase,
                COALESCE(expense.total, 0) AS totalExpense,
                (
                    COALESCE(sale.total, 0) -
                    ROUND(COALESCE(sale.cost, 0), 2) -
                    COALESCE(expense.total, 0)
                ) AS totalProfit
            FROM (
                SELECT
                    COALESCE(SUM(s.orderDiscount), 0) AS discount,
                    COALESCE(SUM(s.orderTax), 0) AS tax,
                    COALESCE(SUM(s.shipping), 0) AS shipping,
                    COALESCE(0, 0) AS refund,
                    COALESCE(SUM(costOfGood.cost), 0) AS cost,
                    COALESCE(SUM(s.grandTotal), 0) AS total
                FROM SaleEntity s
                LEFT JOIN (
                    SELECT
                        _si.saleId AS saleId,
                        SUM(COALESCE(_si.quantity, 0) * COALESCE(_si.baseUnitCost, 0)) AS cost
                    FROM SaleEntity _s
                    INNER JOIN SaleItemEntity _si ON _si.saleId = _s.id
                    WHERE _s.status != 'pending'
                    GROUP BY _si.saleId
                ) costOfGood ON costOfGood.saleId = s.id
                WHERE
                    s.status != 'pending' AND
                    (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR s.createdBy = :#{#filter.user}) AND
                    (:#{#filter.bIds} IS NULL OR s.billerId IN :#{#filter.bIds}) AND
                    (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR s.billerId = :#{#filter.billerId}) AND
                    (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR s.createdBy = :#{#filter.createdBy}) AND
                    (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR s.updatedBy = :#{#filter.updatedBy}) AND
                    (s.date < :#{#filter.start})
            ) sale
            LEFT JOIN (
                SELECT
                    COALESCE(SUM(p.orderDiscount), 0) AS discount,
                    COALESCE(SUM(p.orderTax), 0) AS tax,
                    COALESCE(SUM(p.shipping), 0) AS shipping,
                    COALESCE(0, 0) AS refund,
                    COALESCE(SUM(p.grandTotal), 0) AS total
                FROM PurchaseEntity p
                WHERE
                    p.status != 'pending' AND
                    (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR p.createdBy = :#{#filter.user}) AND
                    (:#{#filter.bIds} IS NULL OR p.billerId IN :#{#filter.bIds}) AND
                    (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR p.billerId = :#{#filter.billerId}) AND
                    (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR p.createdBy = :#{#filter.createdBy}) AND
                    (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR p.updatedBy = :#{#filter.updatedBy}) AND
                    (p.date < :#{#filter.start})
            ) purchase ON 1 = 1
            LEFT JOIN (
                SELECT
                    COALESCE(SUM(xp.amount), 0) AS total
                FROM ExpenseEntity xp
                WHERE
                    (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR xp.createdBy = :#{#filter.user}) AND
                    (:#{#filter.bIds} IS NULL OR xp.billerId IN :#{#filter.bIds}) AND
                    (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR xp.billerId = :#{#filter.billerId}) AND
                    (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR xp.createdBy = :#{#filter.createdBy}) AND
                    (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR xp.updatedBy = :#{#filter.updatedBy}) AND
                    (xp.date < :#{#filter.start})
            ) expense ON 1 = 1
        ) beginning
        LEFT JOIN (
            SELECT
                COALESCE(SUM(s.orderDiscount), 0) AS discount,
                COALESCE(SUM(s.orderTax), 0) AS tax,
                COALESCE(SUM(s.shipping), 0) AS shipping,
                COALESCE(0, 0) AS refund,
                COALESCE(SUM(costOfGood.cost), 0) AS cost,
                COALESCE(SUM(s.grandTotal), 0) AS total
            FROM SaleEntity s
            LEFT JOIN (
                SELECT
                    _si.saleId AS saleId,
                    SUM(COALESCE(_si.quantity, 0) * COALESCE(_si.baseUnitCost, 0)) AS cost
                FROM SaleEntity _s
                INNER JOIN SaleItemEntity _si ON _si.saleId = _s.id
                WHERE _s.status != 'pending'
                GROUP BY _si.saleId
            ) costOfGood ON costOfGood.saleId = s.id
            WHERE
                s.status != 'pending' AND
                (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR s.createdBy = :#{#filter.user}) AND
                (:#{#filter.bIds} IS NULL OR s.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR s.billerId = :#{#filter.billerId}) AND
                (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR s.createdBy = :#{#filter.createdBy}) AND
                (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR s.updatedBy = :#{#filter.updatedBy}) AND
                (s.date BETWEEN :#{#filter.start} AND :#{#filter.end})
        ) sale ON 1 = 1
        LEFT JOIN (
            SELECT
                COALESCE(SUM(p.orderDiscount), 0) AS discount,
                COALESCE(SUM(p.orderTax), 0) AS tax,
                COALESCE(SUM(p.shipping), 0) AS shipping,
                COALESCE(0, 0) AS refund,
                COALESCE(SUM(p.grandTotal), 0) AS total
            FROM PurchaseEntity p
            WHERE
                p.status != 'pending' AND
                (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR p.createdBy = :#{#filter.user}) AND
                (:#{#filter.bIds} IS NULL OR p.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR p.billerId = :#{#filter.billerId}) AND
                (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR p.createdBy = :#{#filter.createdBy}) AND
                (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR p.updatedBy = :#{#filter.updatedBy}) AND
                (p.date BETWEEN :#{#filter.start} AND :#{#filter.end})
        ) purchase ON 1 = 1
        LEFT JOIN (
            SELECT
                COALESCE(SUM(xp.amount), 0) AS total
            FROM ExpenseEntity xp
            WHERE
                (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR xp.createdBy = :#{#filter.user}) AND
                (:#{#filter.bIds} IS NULL OR xp.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR xp.billerId = :#{#filter.billerId}) AND
                (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR xp.createdBy = :#{#filter.createdBy}) AND
                (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR xp.updatedBy = :#{#filter.updatedBy}) AND
                (xp.date BETWEEN :#{#filter.start} AND :#{#filter.end})
        ) expense ON 1 = 1
    """)
    Tuple findProfitAndOrLoss(@Param("filter") PaymentRequest filter);

    @Query(value = """
        SELECT
            COALESCE(SUM(s.orderDiscount), 0) AS discount,
            COALESCE(SUM(s.orderTax), 0) AS tax,
            COALESCE(SUM(s.shipping), 0) AS shipping,
            COALESCE(0, 0) AS refund,
            COALESCE(SUM(s.grandTotal), 0) - COALESCE(SUM(s.orderTax), 0) AS total,
            COALESCE(SUM(s.grandTotal), 0) AS netTotal
        FROM SaleEntity s
        WHERE
            s.status != 'pending' AND
            (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR s.createdBy = :#{#filter.user}) AND
            (:#{#filter.bIds} IS NULL OR s.billerId IN :#{#filter.bIds}) AND
            (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR s.billerId = :#{#filter.billerId}) AND
            (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR s.createdBy = :#{#filter.createdBy}) AND
            (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR s.updatedBy = :#{#filter.updatedBy}) AND
            (s.date BETWEEN :#{#filter.start} AND :#{#filter.end})
    """)
    Tuple findSale(@Param("filter") PaymentRequest filter);

    @Query(value = """
        SELECT
            COALESCE(SUM(p.orderDiscount), 0) AS discount,
            COALESCE(SUM(p.orderTax), 0) AS tax,
            COALESCE(SUM(p.shipping), 0) AS shipping,
            COALESCE(0, 0) AS refund,
            COALESCE(SUM(p.grandTotal), 0) - COALESCE(SUM(p.orderTax), 0) AS total,
            COALESCE(SUM(p.grandTotal), 0) AS netTotal
        FROM PurchaseEntity p
        WHERE
            p.status != 'pending' AND
            (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR p.createdBy = :#{#filter.user}) AND
            (:#{#filter.bIds} IS NULL OR p.billerId IN :#{#filter.bIds}) AND
            (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR p.billerId = :#{#filter.billerId}) AND
            (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR p.createdBy = :#{#filter.createdBy}) AND
            (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR p.updatedBy = :#{#filter.updatedBy}) AND
            (p.date BETWEEN :#{#filter.start} AND :#{#filter.end})
    """)
    Tuple findPurchase(@Param("filter") PaymentRequest filter);

    @Query(value = """
        SELECT
            xp.expenseCategoryId AS id,
            xpc.code AS code,
            xpc.name AS category,
            COALESCE(SUM(xp.amount), 0) AS total
        FROM ExpenseEntity xp
        LEFT JOIN ExpenseCategoryEntity xpc ON xpc.id = xp.expenseCategoryId
        WHERE
            (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR xp.createdBy = :#{#filter.user}) AND
            (:#{#filter.bIds} IS NULL OR xp.billerId IN :#{#filter.bIds}) AND
            (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR xp.billerId = :#{#filter.billerId}) AND
            (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR xp.createdBy = :#{#filter.createdBy}) AND
            (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR xp.updatedBy = :#{#filter.updatedBy}) AND
            (xp.date BETWEEN :#{#filter.start} AND :#{#filter.end})
        GROUP BY xp.expenseCategoryId
    """)
    List<Tuple> findListExpensesGroupByCategory(@Param("filter") PaymentRequest filter);
}
