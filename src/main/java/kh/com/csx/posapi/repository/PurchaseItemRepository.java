package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.entity.PurchaseItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface PurchaseItemRepository extends JpaRepository<PurchaseItemEntity, Long> {
    Optional<PurchaseItemEntity> findById(Long id);

    List<PurchaseItemEntity> findByPurchaseId(Long purchaseId);

    @Modifying
    @Query("DELETE FROM PurchaseItemEntity pi WHERE pi.purchaseId = :purchaseId")
    void deleteByPurchaseId(@Param("purchaseId") Long purchaseId);
}
