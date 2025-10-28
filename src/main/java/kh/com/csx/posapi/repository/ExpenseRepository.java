package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.entity.ExpenseEntity;
import kh.com.csx.posapi.dto.expense.ExpenseRetrieveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {
    boolean existsByReferenceNo(String referenceNo);
    boolean existsByReferenceNoAndIdNot(String referenceNo, Long id);

    @Query("SELECT e FROM ExpenseEntity e " +
            "LEFT JOIN BillerEntity b ON b.id = e.billerId " +
            "LEFT JOIN ExpenseCategoryEntity ec ON ec.id = e.expenseCategoryId " +
            "LEFT JOIN UserEntity u ON u.userId = e.createdBy " +
            "LEFT JOIN EmployeeEntity emp ON emp.id = u.employeeId " +
            "WHERE 1=1 " +
            "AND (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR e.createdBy = :#{#filter.user}) " +
            "AND (:#{#filter.bIds} IS NULL OR e.billerId IN :#{#filter.bIds}) " +
            "AND (:#{#filter.id} IS NULL OR e.id = :#{#filter.id}) " +
            "AND (:#{#filter.referenceNo} IS NULL OR e.referenceNo = :#{#filter.referenceNo}) " +
            "AND (:#{#filter.paymentStatus} IS NULL OR e.paymentStatus = :#{#filter.paymentStatus}) " +
            "AND (:#{#filter.billerId} IS NULL OR e.billerId = :#{#filter.billerId}) " +
            "AND (:#{#filter.expenseCategoryId} IS NULL OR e.expenseCategoryId = :#{#filter.expenseCategoryId}) " +
            "AND (:#{#filter.amount} = 0.0 OR e.amount = :#{#filter.amount}) " +
            "AND (:#{#filter.createdBy} IS NULL OR e.createdBy = :#{#filter.createdBy}) " +
            "AND (:#{#filter.updatedBy} IS NULL OR e.updatedBy = :#{#filter.updatedBy}) " +
            "AND (:#{#filter.start} IS NULL OR e.date >= :#{#filter.start}) " +
            "AND (:#{#filter.end} IS NULL OR e.date <= :#{#filter.end}) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(FUNCTION('DATE_FORMAT', e.date, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(e.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(b.companyEn, ' (', b.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(ec.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(e.paymentStatus) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(e.note) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CASE WHEN emp.id IS NULL THEN u.username ELSE CONCAT(COALESCE(emp.firstName, ''), ' ', COALESCE(emp.lastName, '')) END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    )")
    Page<ExpenseEntity> findAllByFilter(@Param("filter") ExpenseRetrieveRequest filter, Pageable pageable);
}
