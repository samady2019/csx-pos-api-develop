package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.entity.PromotionItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PromotionItemRepository extends JpaRepository<PromotionItemEntity, Long> {
    @Query(value = "SELECT pi.* FROM promotion_items pi INNER JOIN promotions p ON pi.promotion_id = p.id WHERE 1=1 " +
            "AND (:date BETWEEN p.start_date and p.end_date) " +
            "AND (p.billers IS NULL OR TRIM(p.billers) = '' OR FIND_IN_SET(:billerId, p.billers) > 0) " +
            "AND (pi.product_id = :productId) LIMIT 1",
            nativeQuery = true)
    Optional<PromotionItemEntity> findByProduct(Long billerId, Long productId, LocalDate date);

    @Modifying
    @Query("DELETE FROM PromotionItemEntity pi WHERE pi.promotionId = :promotionId")
    void deleteByPromotionId(@Param("promotionId") Long promotionId);
}
