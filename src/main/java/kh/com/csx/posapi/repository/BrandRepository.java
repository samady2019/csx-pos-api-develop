package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.brand.RetrieveBrandDTO;
import kh.com.csx.posapi.entity.BrandEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<BrandEntity, Long> {

    Optional<BrandEntity> findByBrandId(Long brandId);

    Optional<BrandEntity> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndBrandIdNot(String code, Long brandId);

    @Query("SELECT b FROM BrandEntity b WHERE b.status = 1 " +
            "AND (:#{#filter.brandId} IS NULL OR :#{#filter.brandId} = '' OR b.brandId = :#{#filter.brandId}) " +
            "AND (:#{#filter.code} IS NULL OR :#{#filter.code} = '' OR b.code = :#{#filter.code}) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%'))) ")
    List<BrandEntity> findListByFilter(@Param("filter") RetrieveBrandDTO filter);

    @Query("SELECT b FROM BrandEntity b WHERE 1=1 " +
            "AND (:#{#filter.brandId} IS NULL OR :#{#filter.brandId} = '' OR b.brandId = :#{#filter.brandId}) " +
            "AND (:#{#filter.code} IS NULL OR :#{#filter.code} = '' OR b.code = :#{#filter.code}) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%'))) " +
            "AND (:#{#filter.status} IS NULL OR :#{#filter.status} = '' OR b.status = :#{#filter.status}) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(b.code) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(b.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CASE WHEN b.status = 1 THEN 'active' ELSE 'inactive' END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    )")
    Page<BrandEntity> findAllByFilter(@Param("filter") RetrieveBrandDTO filter, Pageable pageable);
}
