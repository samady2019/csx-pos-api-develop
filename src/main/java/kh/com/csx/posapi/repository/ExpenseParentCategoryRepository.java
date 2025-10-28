package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.entity.ExpenseParentCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpenseParentCategoryRepository extends JpaRepository<ExpenseParentCategoryEntity, Long> {

    @Query("SELECT p FROM ExpenseParentCategoryEntity p LEFT JOIN FETCH p.subCategories WHERE p.parentCategory IS NULL")
    List<ExpenseParentCategoryEntity> findAllWithSubCategories();
}
