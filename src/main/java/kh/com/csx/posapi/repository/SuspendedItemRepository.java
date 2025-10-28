package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.entity.SuspendedItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
public interface SuspendedItemRepository extends JpaRepository<SuspendedItemEntity, Long> {
    List<SuspendedItemEntity> findBySuspendId(Long suspendId);

    @Modifying
    @Query("DELETE FROM SuspendedItemEntity si WHERE si.suspendId = :suspendId")
    void deleteBySuspendId(@Param("suspendId") Long suspendId);
}
