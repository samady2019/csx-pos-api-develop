package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.stockCount.StockCountSummary;
import kh.com.csx.posapi.entity.StockCountItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockCountItemRepository extends JpaRepository<StockCountItemEntity, Long> {
    List<StockCountItemEntity> findByStockCountId(Long id);

    @Query(value = "SELECT * FROM stock_count_items WHERE stock_count_id = :id AND product_id = :pId AND (((:exp IS NULL OR :exp = '') AND expiry IS NULL) OR (expiry = :exp)) LIMIT 1", nativeQuery = true)
    Optional<StockCountItemEntity> findByStockCountIdPIDExp(Long id, Long pId, LocalDate exp);

    @Query(value = "SELECT " +
            "        stock_count_id AS id, " +
            "        COUNT(CASE WHEN status = 0 THEN 1 END) AS missing, " +
            "        COUNT(CASE WHEN expected = counted THEN 1 END) AS matches, " +
            "        COUNT(CASE WHEN expected != counted THEN 1 END) AS differences " +
            "    FROM stock_count_items " +
            "    WHERE stock_count_id = :id", nativeQuery = true)
    StockCountSummary getStockCountSummaryById(Long id);

    @Modifying
    @Query("DELETE FROM StockCountItemEntity sci WHERE sci.stockCountId = :id")
    void deleteByStockCountId(Long id);
}
