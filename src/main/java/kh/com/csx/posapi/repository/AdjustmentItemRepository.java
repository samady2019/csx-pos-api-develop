package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.entity.AdjustmentItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AdjustmentItemRepository extends JpaRepository<AdjustmentItemEntity, Long> {
    @Modifying
    @Query(value = "DELETE FROM adjustment_items WHERE adjustment_id = :id", nativeQuery = true)
    void deleteByAdjustmentId(Long id);
}
