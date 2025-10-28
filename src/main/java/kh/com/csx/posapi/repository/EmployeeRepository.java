package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.employee.EmployeeRetrieveRequest;
import kh.com.csx.posapi.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

    Optional<EmployeeEntity> findById(Long id);

    @Query(value = "SELECT e FROM EmployeeEntity e WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR e.id = :#{#filter.id}) " +
            "AND (:#{#filter.firstName} IS NULL OR :#{#filter.firstName} = '' OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :#{#filter.firstName}, '%'))) " +
            "AND (:#{#filter.lastName} IS NULL OR :#{#filter.lastName} = '' OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :#{#filter.lastName}, '%'))) " +
            "AND (:#{#filter.gender} IS NULL OR :#{#filter.gender} = '' OR LOWER(e.gender) = LOWER(:#{#filter.gender})) " +
            "AND (:#{#filter.phone} IS NULL OR :#{#filter.phone} = '' OR e.phone LIKE CONCAT('%', :#{#filter.phone}, '%')) " +
            "AND (:#{#filter.email} IS NULL OR :#{#filter.email} = '' OR e.email LIKE CONCAT('%', :#{#filter.email}, '%')) " +
            "AND (:#{#filter.address} IS NULL OR :#{#filter.address} = '' OR LOWER(e.address) LIKE LOWER(CONCAT('%', :#{#filter.address}, '%'))) " +
            "AND (:#{#filter.nationality} IS NULL OR :#{#filter.nationality} = '' OR LOWER(e.nationality) LIKE LOWER(CONCAT('%', :#{#filter.nationality}, '%'))) ")
    List<EmployeeEntity> findListByFilter(@Param("filter") EmployeeRetrieveRequest filter);

    @Query(value = "SELECT e FROM EmployeeEntity e WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR e.id = :#{#filter.id}) " +
            "AND (:#{#filter.firstName} IS NULL OR :#{#filter.firstName} = '' OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :#{#filter.firstName}, '%'))) " +
            "AND (:#{#filter.lastName} IS NULL OR :#{#filter.lastName} = '' OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :#{#filter.lastName}, '%'))) " +
            "AND (:#{#filter.gender} IS NULL OR :#{#filter.gender} = '' OR LOWER(e.gender) = LOWER(:#{#filter.gender})) " +
            "AND (:#{#filter.phone} IS NULL OR :#{#filter.phone} = '' OR e.phone LIKE CONCAT('%', :#{#filter.phone}, '%')) " +
            "AND (:#{#filter.email} IS NULL OR :#{#filter.email} = '' OR e.email LIKE CONCAT('%', :#{#filter.email}, '%')) " +
            "AND (:#{#filter.address} IS NULL OR :#{#filter.address} = '' OR LOWER(e.address) LIKE LOWER(CONCAT('%', :#{#filter.address}, '%'))) " +
            "AND (:#{#filter.nationality} IS NULL OR :#{#filter.nationality} = '' OR LOWER(e.nationality) LIKE LOWER(CONCAT('%', :#{#filter.nationality}, '%'))) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(e.firstName) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(e.lastName) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(e.gender) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(REPLACE(e.phone, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(TRIM(:#{#filter.term}), ' ', ''), '%')) OR " +
            "           LOWER(e.email) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(e.address) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(e.nationality) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    )")
    Page<EmployeeEntity> findAllByFilter(@Param("filter") EmployeeRetrieveRequest filter, Pageable pageable);
}
