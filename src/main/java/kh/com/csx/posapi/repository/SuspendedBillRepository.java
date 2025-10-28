package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.suspendedBill.SuspendedBillRetrieveRequest;
import kh.com.csx.posapi.entity.SuspendedBillEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
public interface SuspendedBillRepository extends JpaRepository<SuspendedBillEntity, Long> {

    boolean existsByReferenceNo(String referenceNo);

    boolean existsByReferenceNoAndIdNot(String referenceNo, Long id);

    @Query(value = "SELECT s FROM SuspendedBillEntity s " +
            "LEFT JOIN BillerEntity b ON b.id = s.billerId " +
            "LEFT JOIN WarehouseEntity w ON w.id = s.warehouseId " +
            "LEFT JOIN CustomerEntity c ON c.id = s.customerId " +
            "LEFT JOIN UserEntity u ON u.userId = s.createdBy " +
            "LEFT JOIN EmployeeEntity e ON e.id = u.employeeId " +
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
            "AND (:#{#filter.status} IS NULL OR s.status = :#{#filter.status}) " +
            "AND (:#{#filter.salesmanBy} IS NULL OR :#{#filter.salesmanBy} = '' OR s.salesmanBy = :#{#filter.salesmanBy}) " +
            "AND (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR s.createdBy = :#{#filter.createdBy}) " +
            "AND (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR s.updatedBy = :#{#filter.updatedBy}) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(FUNCTION('DATE_FORMAT', s.date, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(b.companyEn, ' (', b.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(w.name, ' (', w.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(c.companyEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(c.nameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CASE WHEN s.status = 0 THEN 'completed' ELSE 'suspended' END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(s.note) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CASE WHEN e.id IS NULL THEN u.username ELSE CONCAT(COALESCE(e.firstName, ''), ' ', COALESCE(e.lastName, '')) END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    ) ")
    Page<SuspendedBillEntity> findAllByFilter(@Param("filter") SuspendedBillRetrieveRequest filter, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "UPDATE suspended_bills SET status = 0 WHERE id = :suspendId", nativeQuery = true)
    void updateStatus(Long suspendId);
}
