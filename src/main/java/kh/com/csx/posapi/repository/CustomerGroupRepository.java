package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.customerGroup.CustomerGroupRetrieveRequest;
import kh.com.csx.posapi.entity.CustomerGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerGroupRepository extends JpaRepository<CustomerGroupEntity, Long> {
    Optional<CustomerGroupEntity> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);                   // Case-insensitive
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);  // Case-insensitive for name, no case sensitivity for id

    @Query(value = "SELECT COUNT(*) > 0 FROM CustomerEntity c WHERE c.customerGroupId = :customerGroupId ")
    boolean existCustomer(Long customerGroupId);

    @Query(value = "SELECT cg FROM CustomerGroupEntity cg WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR cg.id = :#{#filter.id}) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR cg.name = :#{#filter.name}) ")
    List<CustomerGroupEntity> findListByFilter(@Param("filter") CustomerGroupRetrieveRequest filter);

    @Query(value = "SELECT cg FROM CustomerGroupEntity cg WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR cg.id = :#{#filter.id}) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR cg.name = :#{#filter.name}) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(cg.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    )")
    Page<CustomerGroupEntity> findAllByFilter(@Param("filter") CustomerGroupRetrieveRequest filter, Pageable pageable);
}
