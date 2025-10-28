package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.adjustment.AdjustmentRetrieveRequest;
import kh.com.csx.posapi.entity.AdjustmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AdjustmentRepository extends JpaRepository<AdjustmentEntity, Long> {

    Optional<AdjustmentEntity> findById(Long id);

    boolean existsByReferenceNo(String referenceNo);

    boolean existsByReferenceNoAndIdNot(String referenceNo, Long id);

    @Query(value = "SELECT a FROM AdjustmentEntity a " +
            "LEFT JOIN BillerEntity b ON b.id = a.billerId " +
            "LEFT JOIN WarehouseEntity w ON w.id = a.warehouseId " +
            "LEFT JOIN UserEntity u ON u.userId = a.createdBy " +
            "LEFT JOIN EmployeeEntity e ON e.id = u.employeeId " +
            "WHERE 1=1 " +
            "AND (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR a.createdBy = :#{#filter.user}) " +
            "AND (:#{#filter.bIds} IS NULL OR a.billerId IN :#{#filter.bIds}) " +
            "AND (:#{#filter.whIds} IS NULL OR a.warehouseId IN :#{#filter.whIds}) " +
            "AND (:#{#filter.id} IS NULL OR a.id = :#{#filter.id}) " +
            "AND (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (a.date BETWEEN :#{#filter.start} AND :#{#filter.end})) " +
            "AND (:#{#filter.countId} IS NULL OR a.countId = :#{#filter.countId}) " +
            "AND (:#{#filter.referenceNo} IS NULL OR LOWER(a.referenceNo) LIKE LOWER(CONCAT('%', :#{#filter.referenceNo}, '%'))) " +
            "AND (:#{#filter.billerId} IS NULL OR a.billerId = :#{#filter.billerId}) " +
            "AND (:#{#filter.warehouseId} IS NULL OR a.warehouseId = :#{#filter.warehouseId}) " +
            "AND (:#{#filter.createdBy} IS NULL OR a.createdBy = :#{#filter.createdBy}) " +
            "AND (:#{#filter.updatedBy} IS NULL OR a.updatedBy = :#{#filter.updatedBy}) " +
            "AND (:#{#filter.note} IS NULL OR LOWER(a.note) LIKE LOWER(CONCAT('%', :#{#filter.note}, '%'))) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(FUNCTION('DATE_FORMAT', a.date, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(a.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(b.companyEn, ' (', b.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(w.name, ' (', w.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(a.note) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CASE WHEN e.id IS NULL THEN u.username ELSE CONCAT(COALESCE(e.firstName, ''), ' ', COALESCE(e.lastName, '')) END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    ) ")
    Page<AdjustmentEntity> findAllByFilter(@Param("filter") AdjustmentRetrieveRequest filter, Pageable pageable);
}


