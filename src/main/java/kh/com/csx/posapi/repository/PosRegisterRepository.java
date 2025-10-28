package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.posRegister.PosRegisterRetrieveRequest;
import kh.com.csx.posapi.entity.PosRegisterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public interface PosRegisterRepository extends JpaRepository<PosRegisterEntity, Long> {
    @Query(value = "SELECT COUNT(*) > 0 FROM PosRegisterEntity p WHERE p.userId = :userId AND p.status = 'open'")
    boolean existRegister(Long userId);

    @Query("""
        SELECT CASE
            WHEN COUNT(reg) > 0 THEN 'open'
            ELSE 'close'
        END
        FROM PosRegisterEntity reg
        WHERE reg.userId = :userId AND reg.status = 'open'
    """)
    String checkUserRegister(Long userId);

    @Query(value = " " +
            "SELECT COALESCE(SUM(sales.changes), 0) AS total_amount FROM sales " +
            "INNER JOIN pos_register ON pos_register.user_id = :userId AND pos_register.status = 'open' " +
            "WHERE sales.pos = 1 AND sales.created_by = :userId AND sales.date BETWEEN pos_register.date AND CURRENT_TIMESTAMP "
            , nativeQuery = true)
    Double getPosPaymentChangesByUserBill(Long userId);

    @Query(value = " " +
            "SELECT payments.payment_method_id, payment_methods.type, COALESCE(SUM(payments.amount), 0) AS total_amount FROM payments " +
            "INNER JOIN payment_methods ON payment_methods.id = payments.payment_method_id " +
            "INNER JOIN sales ON sales.id = payments.sale_id " +
            "INNER JOIN pos_register ON pos_register.user_id = :userId AND pos_register.status = 'open' " +
            "WHERE sales.pos = 1 AND sales.created_by = :userId AND sales.date BETWEEN pos_register.date AND CURRENT_TIMESTAMP " +
            "GROUP BY payments.payment_method_id "
            , nativeQuery = true)
    List<Map<String, Object>> getPosPaymentsByUserBill(Long userId);

    @Query(value = " " +
            "SELECT payments.payment_method_id, payment_methods.type, COALESCE(SUM(payments.amount), 0) AS total_amount FROM payments " +
            "INNER JOIN payment_methods ON payment_methods.id = payments.payment_method_id " +
            "INNER JOIN expenses ON expenses.id = payments.expense_id " +
            "INNER JOIN pos_register ON pos_register.user_id = :userId AND pos_register.status = 'open' " +
            "WHERE expenses.created_by = :userId AND expenses.date BETWEEN pos_register.date AND CURRENT_TIMESTAMP " +
            "GROUP BY payments.payment_method_id "
            , nativeQuery = true)
    List<Map<String, Object>> getExpensePaymentsByUserBill(Long userId);

    @Query(value = " " +
            "SELECT payments.payment_method_id, payment_methods.type, COALESCE(COUNT(payments.id), 0) AS total FROM payments " +
            "INNER JOIN payment_methods ON payment_methods.id = payments.payment_method_id " +
            "INNER JOIN sales ON sales.id = payments.sale_id " +
            "INNER JOIN pos_register ON pos_register.user_id = :userId AND pos_register.status = 'open' " +
            "WHERE sales.pos = 1 AND sales.created_by = :userId AND sales.date BETWEEN pos_register.date AND CURRENT_TIMESTAMP " +
            "GROUP BY payments.payment_method_id "
            , nativeQuery = true)
    List<Map<String, Object>> getPosTranPaymentsByUserBill(Long userId);

    @Query(value =
            "SELECT r.* FROM pos_register r " +
            "WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR r.id = :#{#filter.id}) " +
            "AND (:#{#filter.userId} IS NULL OR :#{#filter.userId} = '' OR r.user_id = :#{#filter.userId}) " +
            "AND (:#{#filter.closedBy} IS NULL OR :#{#filter.closedBy} = '' OR r.closed_by = :#{#filter.closedBy}) " +
            "AND (:#{#filter.status} IS NULL OR :#{#filter.status} = '' OR r.status = :#{#filter.status}) " +
            "AND (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (r.date BETWEEN :#{#filter.startDate} AND :#{#filter.endDate})) "
            , nativeQuery = true)
    List<PosRegisterEntity> findAllByFilter(@Param("filter") PosRegisterRetrieveRequest filter);
}
