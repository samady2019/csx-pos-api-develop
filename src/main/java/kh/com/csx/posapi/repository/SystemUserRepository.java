package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.auth.UserRetrieveRequest;
import kh.com.csx.posapi.entity.SystemUserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SystemUserRepository extends JpaRepository<SystemUserEntity, Long> {

    Optional<SystemUserEntity> findByUsername(String username);

    Optional<SystemUserEntity> findFirstByUsername(String username);

    Optional<SystemUserEntity> findFirstByUsernameAndUserIdNot(String username, Long user_id);

    Optional<SystemUserEntity> findByUserId(long user_id);

    boolean existsByRoles_RoleId(Integer roleId);

    boolean existsByPermissions_PermissionId(Integer permissionId);

    @Query("SELECT u FROM SystemUserEntity u WHERE u.status = '1'")
    List<SystemUserEntity> findActiveUsers();

    boolean existsByEmployeeId(Long employeeId);

    List<SystemUserEntity> findByEmployeeId(long employeeId);

    @Query(value = "SELECT u " +
            "FROM SystemUserEntity u " +
            "LEFT JOIN EmployeeEntity e ON u.employeeId = e.id " +
            "LEFT JOIN u.roles r " +
            "WHERE 1=1 " +
            "AND (:#{#filter.userId} IS NULL OR :#{#filter.userId} = '' OR u.userId = :#{#filter.userId}) " +
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
    Page<SystemUserEntity> findAllByFilter(@Param("filter") UserRetrieveRequest filter, Pageable pageable);
}
