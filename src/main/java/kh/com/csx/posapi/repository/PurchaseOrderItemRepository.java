package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.entity.PurchaseOrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItemEntity, Long> {
    Optional<PurchaseOrderItemEntity> findById(Long id);

    List<PurchaseOrderItemEntity> findByPurchaseOrderId(Long purchaseOrderId);

    @Modifying
    @Query("DELETE FROM PurchaseOrderItemEntity poi WHERE poi.purchaseOrderId = :purchaseOrderId")
    void deleteByPurchaseOrderId(@Param("purchaseOrderId") Long purchaseOrderId);
}
