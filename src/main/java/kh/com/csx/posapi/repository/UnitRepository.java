package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.unit.UnitRetrieveRequest;
import kh.com.csx.posapi.entity.UnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<UnitEntity, Long> {

    Optional<UnitEntity> findByUnitId(Long unit_id);

    List<UnitEntity> findByPunitId(Long p_unit_id);

    List<UnitEntity> findByPunitIdIsNull();

    Optional<UnitEntity> findByUnitCode(String unit_code);

    Optional<UnitEntity> findFirstByUnitCode(String unit_code);

    Optional<UnitEntity> findByUnitNameEn(String unit_name_en);

    Optional<UnitEntity> findByUnitNameKh(String unit_name_kh);

    Optional<UnitEntity> findByUnitCodeAndUnitIdNot(String unit_code, Long unit_id);

    Optional<UnitEntity> findFirstByUnitCodeAndUnitIdNot(String unit_code, Long unit_id);

    @Query(value = "SELECT units.* FROM product_unit INNER JOIN units ON product_unit.unit_id = units.unit_id WHERE product_unit.product_id = :pId AND (units.p_unit_id IS NULL OR units.p_unit_id = '') LIMIT 1", nativeQuery = true)
    UnitEntity findProductBaseUnit(Long pId);

    @Query(value = "SELECT u FROM UnitEntity u " +
            "LEFT JOIN UnitEntity uu ON uu.unitId = u.punitId " +
            "WHERE 1=1 " +
            "AND (:#{#filter.unitId} IS NULL OR :#{#filter.unitId} = '' OR u.unitId = :#{#filter.unitId}) " +
            "AND (:#{#filter.punitId} IS NULL OR :#{#filter.punitId} = '' OR u.punitId = :#{#filter.punitId}) " +
            "AND (:#{#filter.unitCode} IS NULL OR :#{#filter.unitCode} = '' OR LOWER(u.unitCode) LIKE LOWER(CONCAT('%', :#{#filter.unitCode}, '%'))) " +
            "AND (:#{#filter.unitNameEn} IS NULL OR :#{#filter.unitNameEn} = '' OR LOWER(u.unitNameEn) LIKE LOWER(CONCAT('%', :#{#filter.unitNameEn}, '%'))) " +
            "AND (:#{#filter.unitNameKh} IS NULL OR :#{#filter.unitNameKh} = '' OR LOWER(u.unitNameKh) LIKE LOWER(CONCAT('%', :#{#filter.unitNameKh}, '%'))) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(u.unitCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(u.unitNameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(u.unitNameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(u.description) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(uu.unitCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(uu.unitNameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    )")
    Page<UnitEntity> findAllByFilter(@Param("filter") UnitRetrieveRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            SUM(COALESCE(ref.count, 0)) AS count
        FROM (
            SELECT :unitId AS unitId, COUNT(*) AS count FROM units u         WHERE u.p_unit_id = :unitId
            UNION ALL
            SELECT :unitId AS unitId, COUNT(*) AS count FROM product_unit pu WHERE pu.unit_id = :unitId
        ) ref
    """, nativeQuery = true)
    long countReferences(Long unitId);
}
