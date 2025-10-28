package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.auth.UserRetrieveRequest;
import kh.com.csx.posapi.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findFirstByUsername(String username);

    Optional<UserEntity> findFirstByUsernameAndUserIdNot(String username, Long user_id);

    Optional<UserEntity> findByUserId(long user_id);

    @Query(value = "SELECT u FROM UserEntity u WHERE u.userId =:user_id AND u.userType = '2'")
    Optional<UserEntity> findBySalesmanId(long user_id);

    boolean existsByRoles_RoleId(Integer roleId);

    boolean existsByPermissions_PermissionId(Integer permissionId);

    @Query("SELECT u FROM UserEntity u WHERE u.status = '1' AND u.userType =:userType ")
    List<UserEntity> findActiveUsers(String userType);

    boolean existsByEmployeeId(Long employeeId);

    List<UserEntity> findByEmployeeId(long employeeId);

    @Query(value = "SELECT DISTINCT u " +
            "FROM UserEntity u " +
            "LEFT JOIN EmployeeEntity e ON u.employeeId = e.id " +
            "LEFT JOIN u.roles r " +
            "WHERE 1=1 " +
            "AND (:#{#filter.userId} IS NULL OR :#{#filter.userId} = '' OR u.userId = :#{#filter.userId}) " +
            "AND (:#{#filter.userType} IS NULL OR :#{#filter.userType} = '' OR u.userType = :#{#filter.userType}) " +
            "AND (:#{#filter.username} IS NULL OR :#{#filter.username} = '' OR u.username = :#{#filter.username}) " +
            "AND (:#{#filter.firstName} IS NULL OR :#{#filter.firstName} = '' OR e.firstName LIKE CONCAT('%', :#{#filter.firstName}, '%')) " +
            "AND (:#{#filter.lastName} IS NULL OR :#{#filter.lastName} = '' OR e.lastName LIKE CONCAT('%', :#{#filter.lastName}, '%')) " +
            "AND (:#{#filter.gender} IS NULL OR :#{#filter.gender} = '' OR e.gender = :#{#filter.gender}) " +
            "AND (:#{#filter.phone} IS NULL OR :#{#filter.phone} = '' OR e.phone LIKE CONCAT('%', :#{#filter.phone}, '%')) " +
            "AND (:#{#filter.email} IS NULL OR :#{#filter.email} = '' OR e.email LIKE CONCAT('%', :#{#filter.email}, '%')) " +
            "AND (:#{#filter.status} IS NULL OR :#{#filter.status} = '' OR u.status = :#{#filter.status}) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(u.username) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(e.gender) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(REPLACE(e.phone, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(TRIM(:#{#filter.term}), ' ', ''), '%')) OR " +
            "           LOWER(e.email) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CASE WHEN u.status = '1' THEN 'active' ELSE 'inactive' END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CONCAT(COALESCE(e.firstName, ''), ' ', COALESCE(e.lastName, ''))) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(r.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    ) ")
    Page<UserEntity> findAllByFilter(@Param("filter") UserRetrieveRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            SUM(COALESCE(ref.count, 0)) AS count
        FROM (
            SELECT :userId AS userId, COUNT(prd) AS count FROM ProductEntity prd      WHERE prd.createdBy = :userId OR prd.updatedBy = :userId
            UNION ALL
            SELECT :userId AS userId, COUNT(po)  AS count FROM PurchaseOrderEntity po WHERE po.createdBy = :userId OR po.updatedBy = :userId
            UNION ALL
            SELECT :userId AS userId, COUNT(p)   AS count FROM PurchaseEntity p       WHERE p.createdBy = :userId OR p.updatedBy = :userId
            UNION ALL
            SELECT :userId AS userId, COUNT(s)   AS count FROM SaleEntity s           WHERE s.createdBy = :userId OR s.updatedBy = :userId OR s.salesmanBy = :userId
            UNION ALL
            SELECT :userId AS userId, COUNT(sp)  AS count FROM SuspendedBillEntity sp WHERE sp.createdBy = :userId OR sp.updatedBy = :userId OR sp.salesmanBy = :userId
            UNION ALL
            SELECT :userId AS userId, COUNT(reg) AS count FROM PosRegisterEntity reg  WHERE reg.userId = :userId OR reg.closedBy = :userId
            UNION ALL
            SELECT :userId AS userId, COUNT(aj)  AS count FROM AdjustmentEntity aj    WHERE aj.createdBy = :userId OR aj.updatedBy = :userId
            UNION ALL
            SELECT :userId AS userId, COUNT(tr)  AS count FROM TransferEntity tr      WHERE tr.createdBy = :userId OR tr.updatedBy = :userId
            UNION ALL
            SELECT :userId AS userId, COUNT(sc)  AS count FROM StockCountEntity sc    WHERE sc.createdBy = :userId OR sc.updatedBy = :userId
            UNION ALL
            SELECT :userId AS userId, COUNT(xp)  AS count FROM ExpenseEntity xp       WHERE xp.createdBy = :userId OR xp.updatedBy = :userId
            UNION ALL
            SELECT :userId AS userId, COUNT(pay) AS count FROM PaymentEntity pay      WHERE pay.createdBy = :userId OR pay.updatedBy = :userId
        ) ref
    """)
    long countReferences(Long userId);

















}
