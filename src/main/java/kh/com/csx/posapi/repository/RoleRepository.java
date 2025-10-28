package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.role.RoleRetrieveRequest;
import kh.com.csx.posapi.entity.RoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Integer> {
    List<RoleEntity> findAllById(Iterable<Integer> ids);

    Optional<RoleEntity> findByRoleId(int roleId);

    Optional<RoleEntity> findByName(String name);

    Optional<RoleEntity> findByNameAndRoleIdNot(String name, int roleId);

    boolean existsByPermissions_PermissionId(Integer permissionId);

    @Query(value = "SELECT r FROM RoleEntity r WHERE 1=1 " +
            "AND (:#{#filter.roleId} IS NULL OR :#{#filter.roleId} = '' OR r.roleId = :#{#filter.roleId}) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR LOWER(r.name) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%'))) ")
    List<RoleEntity> findListByFilter(@Param("filter") RoleRetrieveRequest filter);

    @Query(value = "SELECT r FROM RoleEntity r WHERE 1=1 " +
            "AND (:#{#filter.roleId} IS NULL OR :#{#filter.roleId} = '' OR r.roleId = :#{#filter.roleId}) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR LOWER(r.name) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%'))) ")
    Page<RoleEntity> findAllByFilter(@Param("filter") RoleRetrieveRequest filter, Pageable pageable);
}
