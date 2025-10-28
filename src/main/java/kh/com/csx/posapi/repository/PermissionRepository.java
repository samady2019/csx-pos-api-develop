package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Integer> {

    @Query("SELECT p FROM PermissionEntity p WHERE p.permissionId IN :ids")
    List<PermissionEntity> findAllByIds(@Param("ids") Set<Integer> ids);

    Optional<PermissionEntity> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT p FROM PermissionEntity p WHERE p.name LIKE :module")
    List<PermissionEntity> findByModule(String module);
}

