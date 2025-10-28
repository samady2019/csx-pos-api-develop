package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.currencyCalender.CurrencyCalenderRetrieveRequest;
import kh.com.csx.posapi.entity.CurrencyCalenderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyCalenderRepository extends JpaRepository<CurrencyCalenderEntity, Long> {
    Optional<CurrencyCalenderEntity> findById(Long id);

    List<CurrencyCalenderEntity> findByCurrencyId(Long currencyId);

    List<CurrencyCalenderEntity> findByDate(LocalDate date);

    boolean existsByCurrencyIdAndDate(Long currencyId, LocalDate date);

    boolean existsByCurrencyIdAndDateAndIdNot(Long currencyId, LocalDate date, Long id);

    Optional<CurrencyCalenderEntity> findFirstByCurrencyIdAndDate(Long currencyId, LocalDate date);

    @Query(value = "SELECT cl.* FROM currency_calenders cl INNER JOIN currencies c ON cl.currency_id = c.id WHERE c.code = :code AND cl.date = :date", nativeQuery = true)
    Optional<CurrencyCalenderEntity> findFirstByCodeAndDate(String code, LocalDate date);

    @Query(value = "SELECT cl.* FROM currency_calenders cl INNER JOIN currencies c ON cl.currency_id = c.id WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR cl.id = :#{#filter.id}) " +
            "AND (:#{#filter.currencyId} IS NULL OR :#{#filter.currencyId} = '' OR cl.currency_id = :#{#filter.currencyId}) " +
            "AND (:#{#filter.date} IS NULL OR :#{#filter.date} = '' OR cl.date = :#{#filter.date}) " +
            "AND (:#{#filter.code} IS NULL OR :#{#filter.code} = '' OR c.code = :#{#filter.code}) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR c.name = :#{#filter.name}) " +
            "AND (:#{#filter.symbol} IS NULL OR :#{#filter.symbol} = '' OR c.symbol = :#{#filter.symbol}) " ,
            nativeQuery = true)
    List<CurrencyCalenderEntity> findListByFilter(@Param("filter") CurrencyCalenderRetrieveRequest filter);

    @Query(value = "SELECT cl FROM CurrencyCalenderEntity cl " +
            "LEFT JOIN SettingEntity s ON 1=1 " +
            "INNER JOIN CurrencyEntity c ON cl.currencyId = c.id " +
            "INNER JOIN CurrencyEntity b ON s.defaultCurrency = b.code " +
            "WHERE 1=1 " +
            "AND (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR cl.id = :#{#filter.id}) " +
            "AND (:#{#filter.currencyId} IS NULL OR :#{#filter.currencyId} = '' OR cl.currencyId = :#{#filter.currencyId}) " +
            "AND (:#{#filter.date} IS NULL OR cl.date = :#{#filter.date}) " +
            "AND (:#{#filter.code} IS NULL OR :#{#filter.code} = '' OR c.code = :#{#filter.code}) " +
            "AND (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR c.name = :#{#filter.name}) " +
            "AND (:#{#filter.symbol} IS NULL OR :#{#filter.symbol} = '' OR c.symbol = :#{#filter.symbol}) " +
            "AND ( " +
            "      (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') " +
            "      OR ( " +
            "           LOWER(c.code) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(c.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(c.symbol) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(b.code) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(b.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR " +
            "           LOWER(b.symbol) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) " +
            "         ) " +
            "    )")
    Page<CurrencyCalenderEntity> findAllByFilter(@Param("filter") CurrencyCalenderRetrieveRequest filter, Pageable pageable);
}
