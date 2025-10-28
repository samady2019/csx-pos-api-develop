package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.currency.CurrencyRetrieveRequest;
import kh.com.csx.posapi.entity.CurrencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<CurrencyEntity, Long> {
    Optional<CurrencyEntity> findById(Long id);

    Optional<CurrencyEntity> findFirstByCode(String code);

    Optional<CurrencyEntity> findFirstByCodeAndIdNot(String code, Long id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Optional<CurrencyEntity> findFirstByName(String name);

    Optional<CurrencyEntity> findFirstByNameAndIdNot(String name, Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Optional<CurrencyEntity> findFirstBySymbol(String name);

    Optional<CurrencyEntity> findFirstBySymbolAndIdNot(String name, Long id);

    boolean existsBySymbol(String name);

    boolean existsBySymbolAndIdNot(String name, Long id);

    @Query(value = "SELECT c FROM CurrencyEntity c WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR c.id = :#{#filter.id}) " +
            "AND (:#{#filter.code} IS NULL OR :#{#filter.code} = '' OR c.code = :#{#filter.code}) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR c.name = :#{#filter.name}) " +
            "AND (:#{#filter.symbol} IS NULL OR :#{#filter.symbol} = '' OR c.symbol = :#{#filter.symbol}) ")
    List<CurrencyEntity> findListByFilter(@Param("filter") CurrencyRetrieveRequest filter);

    @Query(value = "SELECT c FROM CurrencyEntity c WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR c.id = :#{#filter.id}) " +
            "AND (:#{#filter.code} IS NULL OR :#{#filter.code} = '' OR c.code = :#{#filter.code}) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR c.name = :#{#filter.name}) " +
            "AND (:#{#filter.symbol} IS NULL OR :#{#filter.symbol} = '' OR c.symbol = :#{#filter.symbol}) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(c.code) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(c.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(c.symbol) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    )")
    Page<CurrencyEntity> findAllByFilter(@Param("filter") CurrencyRetrieveRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            SUM(ref.count) AS count
        FROM (
            SELECT COUNT(*) AS count
            FROM purchases_order po
            WHERE
                po.currencies IS NOT NULL AND
                (
                    JSON_UNQUOTE(po.currencies) REGEXP CONCAT('"id":("?', :id, '"?)[^0-9]') OR
                    JSON_UNQUOTE(po.currencies) REGEXP CONCAT('"currencyId":("?', :id, '"?)[^0-9]') OR
                    JSON_UNQUOTE(po.currencies) LIKE CONCAT('%"code":"', :code, '"%')
                )
            UNION ALL
            SELECT COUNT(*) AS count
            FROM purchases p
            WHERE
                p.currencies IS NOT NULL AND
                (
                    JSON_UNQUOTE(p.currencies) REGEXP CONCAT('"id":("?', :id, '"?)[^0-9]') OR
                    JSON_UNQUOTE(p.currencies) REGEXP CONCAT('"currencyId":("?', :id, '"?)[^0-9]') OR
                    JSON_UNQUOTE(p.currencies) LIKE CONCAT('%"code":"', :code, '"%')
                )
            UNION ALL
            SELECT COUNT(*) AS count
            FROM sales s
            WHERE
                s.currencies IS NOT NULL AND
                (
                    JSON_UNQUOTE(s.currencies) REGEXP CONCAT('"id":("?', :id, '"?)[^0-9]') OR
                    JSON_UNQUOTE(s.currencies) REGEXP CONCAT('"currencyId":("?', :id, '"?)[^0-9]') OR
                    JSON_UNQUOTE(s.currencies) LIKE CONCAT('%"code":"', :code, '"%')
                )
            UNION ALL
            SELECT COUNT(*) AS count
            FROM expenses xp
            WHERE
                xp.currencies IS NOT NULL AND
                (
                    JSON_UNQUOTE(xp.currencies) REGEXP CONCAT('"id":("?', :id, '"?)[^0-9]') OR
                    JSON_UNQUOTE(xp.currencies) REGEXP CONCAT('"currencyId":("?', :id, '"?)[^0-9]') OR
                    JSON_UNQUOTE(xp.currencies) LIKE CONCAT('%"code":"', :code, '"%')
                )
            UNION ALL
            SELECT COUNT(*) AS count
            FROM payments pay
            WHERE
                pay.currencies IS NOT NULL AND
                (
                    JSON_UNQUOTE(pay.currencies) REGEXP CONCAT('"id":("?', :id, '"?)[^0-9]') OR
                    JSON_UNQUOTE(pay.currencies) REGEXP CONCAT('"currencyId":("?', :id, '"?)[^0-9]') OR
                    JSON_UNQUOTE(pay.currencies) LIKE CONCAT('%"code":"', :code, '"%')
                )
        ) ref
    """, nativeQuery = true)
    long countReferences(@Param("id") Long id, @Param("code") String code);

    @Query(value = """
        SELECT
            SUM(COALESCE(ref.count, 0)) AS count
        FROM (
            SELECT COUNT(*) AS count
            FROM purchases_order po
            JOIN JSON_TABLE(
                CAST(JSON_UNQUOTE(po.currencies) AS JSON),
                    '$[*]' COLUMNS (
                        id INT PATH '$.currencyCalender.currency.id',
                        code VARCHAR(20) PATH '$.currencyCalender.currency.code'
                    )
                ) AS jt
            WHERE po.currencies IS NOT NULL AND JSON_VALID(JSON_UNQUOTE(po.currencies)) = 1 AND (jt.id = :id OR jt.code = :code)
            UNION ALL
            SELECT COUNT(*) AS count
            FROM purchases p
            JOIN JSON_TABLE(
                CAST(JSON_UNQUOTE(p.currencies) AS JSON),
                    '$[*]' COLUMNS (
                        id INT PATH '$.currencyCalender.currency.id',
                        code VARCHAR(20) PATH '$.currencyCalender.currency.code'
                    )
                ) AS jt
            WHERE p.currencies IS NOT NULL AND JSON_VALID(JSON_UNQUOTE(p.currencies)) = 1 AND (jt.id = :id OR jt.code = :code)
            UNION ALL
            SELECT COUNT(*) AS count
            FROM sales s
            JOIN JSON_TABLE(
                CAST(JSON_UNQUOTE(s.currencies) AS JSON),
                    '$[*]' COLUMNS (
                        id INT PATH '$.currencyCalender.currency.id',
                        code VARCHAR(20) PATH '$.currencyCalender.currency.code'
                    )
                ) AS jt
            WHERE s.currencies IS NOT NULL AND JSON_VALID(JSON_UNQUOTE(s.currencies)) = 1 AND (jt.id = :id OR jt.code = :code)
            UNION ALL
            SELECT COUNT(*) AS count
            FROM expenses xp
            JOIN JSON_TABLE(
                CAST(JSON_UNQUOTE(xp.currencies) AS JSON),
                    '$[*]' COLUMNS (
                        id INT PATH '$.currencyCalender.currency.id',
                        code VARCHAR(20) PATH '$.currencyCalender.currency.code'
                    )
                ) AS jt
            WHERE xp.currencies IS NOT NULL AND JSON_VALID(JSON_UNQUOTE(xp.currencies)) = 1 AND (jt.id = :id OR jt.code = :code)
            UNION ALL
            SELECT COUNT(*) AS count
            FROM payments pay
            JOIN JSON_TABLE(
                CAST(JSON_UNQUOTE(pay.currencies) AS JSON),
                    '$[*]' COLUMNS (
                        code VARCHAR(20) PATH '$.code'
                    )
                ) AS jt
            WHERE pay.currencies IS NOT NULL AND JSON_VALID(JSON_UNQUOTE(pay.currencies)) = 1 AND (jt.code = :code)
        ) ref
    """, nativeQuery = true)
    long countReferencesJson(Long id, String code);
}
