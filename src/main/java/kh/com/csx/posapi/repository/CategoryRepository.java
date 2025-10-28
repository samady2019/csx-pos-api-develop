package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.category.CategoryRetrieveRequest;
import kh.com.csx.posapi.entity.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    boolean existsByCode(String code);
    boolean existsByCodeAndCategoryIdNot(String code, Long id);
    Optional<CategoryEntity> findByCategoryId(Long categoryId);
    Optional<CategoryEntity> findByCode(String code);
    List<CategoryEntity> findByParentCategoryCategoryId(Long parentId);
    List<CategoryEntity> findByParentCategoryIsNull();

    @Query("SELECT c FROM CategoryEntity c WHERE c.status = 1 " +
            "AND (:#{#filter.categoryId} IS NULL OR :#{#filter.categoryId} = '' OR c.categoryId = :#{#filter.categoryId}) " +
            "AND (:#{#filter.pCategoryId} IS NULL OR :#{#filter.pCategoryId} = '' OR c.pCategoryId = :#{#filter.pCategoryId}) " +
            "AND (:#{#filter.code} IS NULL OR :#{#filter.code} = '' OR c.code = :#{#filter.code}) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%'))) " +
            "AND (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR c.createdBy = :#{#filter.createdBy}) " +
            "AND (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR c.updatedBy = :#{#filter.updatedBy}) ")
    List<CategoryEntity> findListByFilter(@Param("filter") CategoryRetrieveRequest filter);

    @Query("SELECT c FROM CategoryEntity c WHERE c.status = 1 " +
            "AND (c.parentCategory IS NULL) " +
            "AND (:#{#filter.categoryId} IS NULL OR :#{#filter.categoryId} = '' OR c.categoryId = :#{#filter.categoryId}) " +
            "AND (:#{#filter.code} IS NULL OR :#{#filter.code} = '' OR c.code = :#{#filter.code}) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%'))) " +
            "AND (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR c.createdBy = :#{#filter.createdBy}) " +
            "AND (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR c.updatedBy = :#{#filter.updatedBy}) ")
    List<CategoryEntity> findAllParentByFilter(@Param("filter") CategoryRetrieveRequest filter);

    @Query("SELECT c FROM CategoryEntity c " +
            "LEFT JOIN CategoryEntity cc ON cc.categoryId = c.pCategoryId " +
            "WHERE 1=1 " +
            "AND (:#{#filter.categoryId} IS NULL OR :#{#filter.categoryId} = '' OR c.categoryId = :#{#filter.categoryId}) " +
            "AND (:#{#filter.pCategoryId} IS NULL OR :#{#filter.pCategoryId} = '' OR c.pCategoryId = :#{#filter.pCategoryId}) " +
            "AND (:#{#filter.code} IS NULL OR :#{#filter.code} = '' OR c.code = :#{#filter.code}) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%'))) " +
            "AND (:#{#filter.status} IS NULL OR :#{#filter.status} = '' OR c.status = :#{#filter.status}) " +
            "AND (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR c.createdBy = :#{#filter.createdBy}) " +
            "AND (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR c.updatedBy = :#{#filter.updatedBy}) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(c.code) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(c.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(c.description) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(cc.code) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(cc.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(CASE WHEN c.status = 1 THEN 'active' ELSE 'inactive' END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    )")
    Page<CategoryEntity> findAllByFilter(@Param("filter") CategoryRetrieveRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            SUM(COALESCE(ref.count, 0)) AS count
        FROM (
            SELECT :categoryId AS categoryId, COUNT(*) AS count FROM products p   WHERE p.category_id = :categoryId
            UNION ALL
            SELECT :categoryId AS categoryId, COUNT(*) AS count FROM categories c WHERE c.p_category_id = :categoryId
        ) ref
    """, nativeQuery = true)
    long countReferences(Long categoryId);
}
