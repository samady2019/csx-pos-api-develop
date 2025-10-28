package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.stockCount.StockCountRetrieveRequest;
import kh.com.csx.posapi.entity.StockCountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockCountRepository extends JpaRepository<StockCountEntity, Long> {

    boolean existsByReferenceNo(String referenceNo);

    boolean existsByReferenceNoAndIdNot(String referenceNo, Long id);

    @Query(value = "SELECT sc FROM StockCountEntity sc " +
            "LEFT JOIN BillerEntity b ON b.id = sc.billerId " +
            "LEFT JOIN WarehouseEntity w ON w.id = sc.warehouseId " +
            "LEFT JOIN UserEntity u ON u.userId = sc.createdBy " +
            "LEFT JOIN EmployeeEntity e ON e.id = u.employeeId " +
            "WHERE 1=1 " +
            "AND (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR sc.createdBy = :#{#filter.user}) " +
            "AND (:#{#filter.bIds} IS NULL OR sc.billerId IN :#{#filter.bIds}) " +
            "AND (:#{#filter.whIds} IS NULL OR sc.warehouseId IN :#{#filter.whIds}) " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR sc.id = :#{#filter.id}) " +
            "AND (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (sc.date BETWEEN :#{#filter.start} AND :#{#filter.end})) " +
            "AND (:#{#filter.referenceNo} IS NULL OR :#{#filter.referenceNo} = '' OR sc.referenceNo = :#{#filter.referenceNo}) " +
            "AND (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR sc.billerId = :#{#filter.billerId}) " +
            "AND (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR sc.warehouseId = :#{#filter.warehouseId}) " +
            "AND (:#{#filter.type} IS NULL OR :#{#filter.type} = '' OR sc.type = :#{#filter.type}) " +
            "AND (:#{#filter.status} IS NULL OR :#{#filter.status} = '' OR sc.status = :#{#filter.status}) " +
            "AND (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR sc.createdBy = :#{#filter.createdBy}) " +
            "AND (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR sc.updatedBy = :#{#filter.updatedBy}) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(FUNCTION('DATE_FORMAT', sc.date, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(sc.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(b.companyEn, ' (', b.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(w.name, ' (', w.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(sc.type) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(sc.status) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CASE WHEN e.id IS NULL THEN u.username ELSE CONCAT(COALESCE(e.firstName, ''), ' ', COALESCE(e.lastName, '')) END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    ) ")
    Page<StockCountEntity> findAllByFilter(@Param("filter") StockCountRetrieveRequest filter, Pageable pageable);
}
