package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.expenseCategory.ExpenseCategoryRetrieveRequest;
import kh.com.csx.posapi.entity.ExpenseCategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategoryEntity, Long> {
    boolean existsByCode(String code);
    boolean existsByParentCategory(ExpenseCategoryEntity parentCategory);

    @Query("SELECT e FROM ExpenseCategoryEntity e " +
            "LEFT JOIN ExpenseCategoryEntity ee ON ee.id = e.parentCategory.id " +
            "WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR e.id = :#{#filter.id}) " +
            "AND (:#{#filter.code} IS NULL OR e.code LIKE %:#{#filter.code}%) " +
            "AND (:#{#filter.name} IS NULL OR e.name LIKE %:#{#filter.name}%) " +
            "AND (:#{#filter.parentId} IS NULL OR e.parentCategory.id = :#{#filter.parentId}) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(e.code) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(e.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(e.description) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(ee.code) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(ee.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    )")
    Page<ExpenseCategoryEntity> findAllByFilter(@Param("filter") ExpenseCategoryRetrieveRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            SUM(COALESCE(ref.count, 0)) AS count
        FROM (
            SELECT :id AS id, COUNT(*) AS count FROM expense_categories xpc WHERE xpc.parent_id = :id
            UNION ALL
            SELECT :id AS id, COUNT(*) AS count FROM expenses xp            WHERE xp.expense_category_id = :id
        ) ref
    """, nativeQuery = true)
    long countReferences(Long id);
}
