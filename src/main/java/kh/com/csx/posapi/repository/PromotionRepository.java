package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.promotion.PromotionRetrieveRequest;
import kh.com.csx.posapi.entity.PromotionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {
    @Query(value = "SELECT p FROM PromotionEntity p WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR p.id = :#{#filter.id}) " +
            "AND (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR CONCAT(',', p.billers, ',') LIKE CONCAT('%,', :#{#filter.billerId}, ',%')) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%'))) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(p.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(p.description) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    )")
    Page<PromotionEntity> findAllByFilter(@Param("filter") PromotionRetrieveRequest filter, Pageable pageable);
}
