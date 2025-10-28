package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.vendor.VendorRetrieveRequest;
import kh.com.csx.posapi.entity.VendorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<VendorEntity, Long> {
    Optional<VendorEntity> findById(Long id);

    @Query(value = "SELECT v.* FROM vendors v WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR v.id = :#{#filter.id}) " +
            "AND (:#{#filter.userId} IS NULL OR :#{#filter.userId} = '' OR v.user_id = :#{#filter.userId}) " +
            "AND (:#{#filter.firstName} IS NULL OR :#{#filter.firstName} = '' OR v.first_name LIKE CONCAT('%', :#{#filter.firstName}, '%')) " +
            "AND (:#{#filter.lastName} IS NULL OR :#{#filter.lastName} = '' OR v.last_name LIKE CONCAT('%', :#{#filter.lastName}, '%')) " +
            "AND (:#{#filter.shopNameEn} IS NULL OR :#{#filter.shopNameEn} = '' OR v.shop_name_en LIKE CONCAT('%', :#{#filter.shopNameEn}, '%')) " +
            "AND (:#{#filter.shopNameKh} IS NULL OR :#{#filter.shopNameKh} = '' OR v.shop_name_kh LIKE CONCAT('%', :#{#filter.shopNameKh}, '%')) ",
            nativeQuery = true)
    List<VendorEntity> findListByFilter(@Param("filter") VendorRetrieveRequest filter);

    @Query(value = "SELECT v FROM VendorEntity v WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR v.id = :#{#filter.id}) " +
            "AND (:#{#filter.userId} IS NULL OR :#{#filter.userId} = '' OR v.userId = :#{#filter.userId}) " +
            "AND (:#{#filter.firstName} IS NULL OR :#{#filter.firstName} = '' OR v.firstName LIKE CONCAT('%', :#{#filter.firstName}, '%')) " +
            "AND (:#{#filter.lastName} IS NULL OR :#{#filter.lastName} = '' OR v.lastName LIKE CONCAT('%', :#{#filter.lastName}, '%')) " +
            "AND (:#{#filter.shopNameEn} IS NULL OR :#{#filter.shopNameEn} = '' OR v.shopNameEn LIKE CONCAT('%', :#{#filter.shopNameEn}, '%')) " +
            "AND (:#{#filter.shopNameKh} IS NULL OR :#{#filter.shopNameKh} = '' OR v.shopNameKh LIKE CONCAT('%', :#{#filter.shopNameKh}, '%')) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(v.firstName) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(v.lastName) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(v.shopNameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(v.shopNameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    )")
    Page<VendorEntity> findAllByFilter(@Param("filter") VendorRetrieveRequest filter, Pageable pageable);
}
