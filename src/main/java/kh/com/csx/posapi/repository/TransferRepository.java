package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.transfer.TransferRetrieveRequest;
import kh.com.csx.posapi.entity.TransferEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferRepository extends JpaRepository<TransferEntity, Long> {

    boolean existsByReferenceNo(String referenceNo);

    boolean existsByReferenceNoAndIdNot(String referenceNo, Long id);

    @Query("SELECT t FROM TransferEntity t " +
            "LEFT JOIN BillerEntity fb ON fb.id = t.fromBiller " +
            "LEFT JOIN BillerEntity tb ON tb.id = t.toBiller " +
            "LEFT JOIN WarehouseEntity fw ON fw.id = t.fromWarehouse " +
            "LEFT JOIN WarehouseEntity tw ON tw.id = t.toWarehouse " +
            "LEFT JOIN UserEntity u ON u.userId = t.createdBy " +
            "LEFT JOIN EmployeeEntity e ON e.id = u.employeeId " +
            "WHERE 1=1 " +
            "AND (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR t.createdBy = :#{#filter.user}) " +
            "AND (:#{#filter.bIds} IS NULL OR (t.fromBiller IN :#{#filter.bIds} OR t.toBiller IN :#{#filter.bIds})) " +
            "AND (:#{#filter.whIds} IS NULL OR (t.fromWarehouse IN :#{#filter.whIds} OR t.toWarehouse IN :#{#filter.whIds})) " +
            "AND (:#{#filter.id} IS NULL OR t.id = :#{#filter.id}) " +
            "AND (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (t.date BETWEEN :#{#filter.start} AND :#{#filter.end})) " +
            "AND (:#{#filter.status} IS NULL OR t.status = :#{#filter.status}) " +
            "AND (:#{#filter.warehouseId} IS NULL OR (t.fromWarehouse = :#{#filter.warehouseId} OR t.toWarehouse = :#{#filter.warehouseId})) " +
            "AND (:#{#filter.billerId} IS NULL OR (t.fromBiller = :#{#filter.billerId} OR t.toBiller = :#{#filter.billerId})) " +
            "AND (:#{#filter.createdBy} IS NULL OR t.createdBy = :#{#filter.createdBy}) " +
            "AND (:#{#filter.updatedBy} IS NULL OR t.updatedBy = :#{#filter.updatedBy}) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(FUNCTION('DATE_FORMAT', t.date, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(t.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(fb.companyEn, ' (', fb.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(tb.companyEn, ' (', tb.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(fw.name, ' (', fw.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(tw.name, ' (', tw.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(t.status) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(t.note) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CASE WHEN e.id IS NULL THEN u.username ELSE CONCAT(COALESCE(e.firstName, ''), ' ', COALESCE(e.lastName, '')) END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    ) ")
    Page<TransferEntity> findAllByFilter(@Param("filter") TransferRetrieveRequest filter, Pageable pageable);
}
