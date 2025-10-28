package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.language.LanguageRetrieveRequest;
import kh.com.csx.posapi.entity.LanguageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<LanguageEntity, Long> {
    Optional<LanguageEntity> findFirstByCode(String code);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);

    @Query(value = """
        SELECT id, code,
            CASE WHEN :langCode IS NULL OR :langCode = '' THEN khmer END AS khmer,
            CASE WHEN :langCode IS NULL OR :langCode = '' THEN english END AS english,
            CASE WHEN :langCode IS NULL OR :langCode = '' THEN chinese END AS chinese,
            CASE WHEN :langCode IS NULL OR :langCode = '' THEN thai END AS thai,
            CASE WHEN :langCode IS NULL OR :langCode = '' THEN vietnamese END AS vietnamese,
            IF(:langCode = 'kh', khmer, NULL) AS khmer,
            IF(:langCode = 'en', english, NULL) AS english,
            IF(:langCode = 'cn', chinese, NULL) AS chinese,
            IF(:langCode = 'th', thai, NULL) AS thai,
            IF(:langCode = 'vn', vietnamese, NULL) AS vietnamese
        FROM language
    """, nativeQuery = true)
    List<Map<String, Object>> findLanguagesByLangCode(@Param("langCode") String langCode);

    @Query(value = """
        SELECT l FROM LanguageEntity l WHERE 1=1
        AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR l.id = :#{#filter.id})
        AND (:#{#filter.code} IS NULL OR :#{#filter.code} = '' OR l.code = :#{#filter.code})
        AND (
                :#{#filter.term} IS NULL OR :#{#filter.term} = '' OR
                (
                    LOWER(COALESCE(CAST(l.khmer AS string), '')) LIKE LOWER(CONCAT('%', :#{#filter.term}, '%')) OR
                    LOWER(COALESCE(CAST(l.english AS string), '')) LIKE LOWER(CONCAT('%', :#{#filter.term}, '%')) OR
                    LOWER(COALESCE(CAST(l.chinese AS string), '')) LIKE LOWER(CONCAT('%', :#{#filter.term}, '%')) OR
                    LOWER(COALESCE(CAST(l.thai AS string), '')) LIKE LOWER(CONCAT('%', :#{#filter.term}, '%')) OR
                    LOWER(COALESCE(CAST(l.vietnamese AS string), '')) LIKE LOWER(CONCAT('%', :#{#filter.term}, '%'))
                )
            )
    """)
    Page<LanguageEntity> findAllByFilter(@Param("filter") LanguageRetrieveRequest filter, Pageable pageable);
}
