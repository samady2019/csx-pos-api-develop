package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.payment.PaymentRetrieveRequest;
import kh.com.csx.posapi.entity.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findById(Long id);

    List<PaymentEntity> findByPurchaseOrderId(Long id);

    List<PaymentEntity> findByPurchaseId(Long id);

    List<PaymentEntity> findByExpenseId(Long id);

    List<PaymentEntity> findBySaleOrderId(Long id);

    List<PaymentEntity> findBySaleId(Long id);

    boolean existsByReferenceNo(String referenceNo);

    boolean existsByReferenceNoAndIdNot(String referenceNo, Long id);

    @Query("SELECT SUM(p.amount) FROM PaymentEntity p WHERE p.purchaseId = :purchaseId")
    Double getTotalPaidByPurchaseId(@Param("purchaseId") Long purchaseId);

    @Query(value = """
        SELECT 
        COALESCE
        (
            (
                SELECT COALESCE(pays.amount, 0) - COALESCE(ppays.amount, 0) AS amount
                FROM purchases_order
                LEFT JOIN (
                    SELECT COALESCE(SUM(payments.amount), 0) AS amount
                    FROM payments
                    WHERE  
                        payments.purchase_order_id = :purchaseOrderId
                ) pays ON 1=1
                LEFT JOIN (
                    SELECT SUM(ppay.amount) AS amount
                    FROM purchases
                    LEFT JOIN (
                        SELECT payments.purchase_id, SUM(payments.amount) AS amount
                        FROM payments 
                        WHERE 
                            payments.purchase_id IS NOT NULL AND 
                            payments.payment_method_id = 7 AND
                            payments.type != 'returned'
                        GROUP BY payments.purchase_id
                    ) ppay ON ppay.purchase_id = purchases.id 
                    WHERE purchases.purchase_order_id = :purchaseOrderId
                ) ppays ON 1=1
                WHERE purchases_order.id = :purchaseOrderId
            ), 
            0
        )    
    """, nativeQuery = true)
    Double getTotalDepositByPurchaseOrderId(Long purchaseOrderId);

    @Query(value = """
        SELECT 
        COALESCE
        (
            (
                SELECT COALESCE(pays.amount, 0) - (-1* COALESCE(rpays.amount, 0)) AS amount
                FROM purchases 
                LEFT JOIN (
                    SELECT COALESCE(SUM(payments.amount), 0) AS amount
                    FROM payments
                    WHERE 
                        payments.purchase_id = :purchaseId AND 
                        payments.payment_method_id = 7 AND
                        payments.type != 'returned'
                ) pays ON 1=1
                LEFT JOIN (
                    SELECT SUM(rpay.amount) AS amount
                    FROM purchases
                    LEFT JOIN (
                        SELECT payments.purchase_id, SUM(payments.amount) AS amount
                        FROM payments 
                        WHERE 
                            payments.purchase_id IS NOT NULL AND 
                            payments.payment_method_id = 7 AND
                            payments.type = 'returned'
                        GROUP BY payments.purchase_id
                    ) rpay ON rpay.purchase_id = purchases.id 
                    WHERE purchases.purchase_id = :purchaseId
                ) rpays ON 1=1
                WHERE purchases.id = :purchaseId AND purchases.status != 'returned'
            ), 
            0
        )
    """, nativeQuery = true)
    Double getTotalDepositByPurchaseId(Long purchaseId);

    @Query(value = "SELECT p FROM PaymentEntity p " +
            "LEFT JOIN BillerEntity b ON b.id = p.billerId " +
            "LEFT JOIN PaymentMethodEntity pm ON pm.id = p.paymentMethodId " +
            "LEFT JOIN PurchaseOrderEntity po ON po.id = p.purchaseOrderId " +
            "LEFT JOIN PurchaseEntity pu ON pu.id = p.purchaseId " +
            "LEFT JOIN SaleEntity s ON s.id = p.saleId " +
            "LEFT JOIN ExpenseEntity e ON e.id = p.expenseId " +
            "LEFT JOIN UserEntity u ON u.userId = p.createdBy " +
            "LEFT JOIN EmployeeEntity emp ON emp.id = u.employeeId " +
            "WHERE 1=1 " +
            "AND (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR p.createdBy = :#{#filter.user}) " +
            "AND (:#{#filter.bIds} IS NULL OR p.billerId IN :#{#filter.bIds}) " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR p.id = :#{#filter.id}) " +
            "AND (:#{#filter.referenceNo} IS NULL OR :#{#filter.referenceNo} = '' OR p.referenceNo = :#{#filter.referenceNo}) " +
            "AND (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR p.billerId = :#{#filter.billerId}) " +
            "AND (:#{#filter.saleOrderId} IS NULL OR :#{#filter.saleOrderId} = '' OR p.saleOrderId = :#{#filter.saleOrderId}) " +
            "AND (:#{#filter.saleId} IS NULL OR :#{#filter.saleId} = '' OR p.saleId = :#{#filter.saleId}) " +
            "AND (:#{#filter.purchaseOrderId} IS NULL OR :#{#filter.purchaseOrderId} = '' OR p.purchaseOrderId = :#{#filter.purchaseOrderId}) " +
            "AND (:#{#filter.purchaseId} IS NULL OR :#{#filter.purchaseId} = '' OR p.purchaseId = :#{#filter.purchaseId}) " +
            "AND (:#{#filter.paymentMethodId} IS NULL OR :#{#filter.paymentMethodId} = '' OR p.paymentMethodId = :#{#filter.paymentMethodId}) " +
            "AND (:#{#filter.type} IS NULL OR :#{#filter.type} = '' OR p.type = :#{#filter.type}) " +
            "AND (:#{#filter.tranPurchaseOrder} IS NULL OR :#{#filter.tranPurchaseOrder} = '' OR (p.purchaseOrderId IS NOT NULL)) " +
            "AND (:#{#filter.tranPurchase} IS NULL OR :#{#filter.tranPurchase} = '' OR (p.purchaseId IS NOT NULL)) " +
            "AND (:#{#filter.tranSaleOrder} IS NULL OR :#{#filter.tranSaleOrder} = '' OR (p.saleOrderId IS NOT NULL)) " +
            "AND (:#{#filter.tranSale} IS NULL OR :#{#filter.tranSale} = '' OR (p.saleId IS NOT NULL)) " +
            "AND (:#{#filter.tranExpense} IS NULL OR :#{#filter.tranExpense} = '' OR (p.expenseId IS NOT NULL)) " +
            "AND (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (p.date BETWEEN :#{#filter.start} AND :#{#filter.end})) " +
            "AND (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR p.createdBy = :#{#filter.createdBy}) " +
            "AND (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR p.updatedBy = :#{#filter.updatedBy}) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(FUNCTION('DATE_FORMAT', p.date, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(p.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(po.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(pu.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(e.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(b.companyEn, ' (', b.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(pm.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(p.type) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(p.note) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CASE WHEN emp.id IS NULL THEN u.username ELSE CONCAT(COALESCE(emp.firstName, ''), ' ', COALESCE(emp.lastName, '')) END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    )")
    Page<PaymentEntity> findAllByFilter(@Param("filter") PaymentRetrieveRequest filter, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "CALL UpdatePaymentStatus(:purchaseOrderId, :purchaseId, :saleOrderId, :saleId, :expenseId)", nativeQuery = true)
    void updatePaymentStatus(Long purchaseOrderId, Long purchaseId, Long saleOrderId, Long saleId, Long expenseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PaymentEntity p WHERE p.purchaseOrderId = :purchaseOrderId")
    void deletePaymentsByPurchaseOrderId(@Param("purchaseOrderId") Long purchaseOrderId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PaymentEntity p WHERE p.purchaseId = :purchaseId")
    void deletePaymentsByPurchaseId(@Param("purchaseId") Long purchaseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PaymentEntity p WHERE p.saleOrderId = :saleOrderId")
    void deletePaymentsBySaleOrderId(@Param("saleOrderId") Long saleOrderId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PaymentEntity p WHERE p.saleId = :saleId")
    void deletePaymentsBySaleId(@Param("saleId") Long saleId);
}
