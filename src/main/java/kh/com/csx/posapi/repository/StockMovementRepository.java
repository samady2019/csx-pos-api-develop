package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.dto.stockCount.StockCountItem;
import kh.com.csx.posapi.entity.StockMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovementEntity, Long> {
    Optional<StockMovementEntity> findById(Long id);

    List<StockMovementEntity> findByProductId(Long productId);

    void deleteByTransactionId(Long transactionId);

    @Modifying
    @Query("DELETE FROM StockMovementEntity sm WHERE sm.transaction = :tran AND sm.transactionId = :id")
    void deleteByTransactionAndTransactionId(String tran, Long id);

    @Query("SELECT sm FROM StockMovementEntity sm WHERE sm.transaction = :tran AND sm.transactionId = :id")
    List<StockMovementEntity> findByTransactionAndTransactionId(String tran, Long id);

    @Query(value = "SELECT stock_movement.* FROM stock_movement " +
            "WHERE product_id = :pId " +
            "AND ((:tran IS NULL OR :tran = '') OR NOT (transaction = :tran AND transaction_id = :tranId)) " +
            "ORDER BY date :order, id :order",
            nativeQuery = true)
    List<StockMovementEntity> findStkTnx(Long pId, String tran, Long tranId, String order);

    @Query(value = "SELECT id, date, transaction, transaction_id, warehouse_id, product_id, unit_id, unit_quantity, SUM(quantity) AS quantity, expiry, cost FROM stock_movement " +
            "WHERE product_id = :pId " +
            "AND warehouse_id = :whId " +
            "AND ((:exp IS NULL AND expiry IS NULL) OR expiry = :exp) " +
            "AND ((:tran IS NULL OR :tran = '') OR (transaction = :tran AND transaction_id != :tranId)) " +
            "GROUP BY expiry " +
            "LIMIT 1 ",
            nativeQuery = true)
    StockMovementEntity findProductExpiryByProductId(Long whId, Long pId, LocalDate exp, String tran, Long tranId);

    @Query(value = "SELECT id, date, transaction, transaction_id, warehouse_id, product_id, unit_id, unit_quantity, SUM(quantity) AS quantity, expiry, cost FROM stock_movement " +
            "WHERE product_id = :pId " +
            "AND ((:whId IS NULL OR :whId = '') OR warehouse_id = :whId) " +
            "AND ((:exp IS NULL OR :exp = '') OR expiry = :exp) " +
            "AND ((:tran IS NULL OR :tran = '') OR (transaction = :tran AND transaction_id != :tranId)) " +
            "GROUP BY expiry " +
            "ORDER BY expiry ASC ",
            nativeQuery = true)
    List<StockMovementEntity> findProductExpiriesByProductId(Long whId, Long pId, LocalDate exp, String tran, Long tranId);

    @Query(value = "SELECT sm.id, sm.date, sm.transaction, sm.transaction_id, sm.warehouse_id, sm.product_id, sm.unit_id, sm.unit_quantity, SUM(sm.quantity) AS quantity, sm.expiry, sm.cost FROM stock_movement sm " +
            "INNER JOIN products ON products.product_id = sm.product_id AND products.status = 1 AND products.type = 'standard' " +
            "WHERE 1=1 " +
            "AND ((:whId IS NULL OR :whId = '') OR sm.warehouse_id = :whId) " +
            "AND ((:pId IS NULL OR :pId = '') OR sm.product_id = :pId) " +
            "AND ((:bndIds IS NULL OR :bndIds = '') OR products.brand_id IN (:bndIds)) " +
            "AND ((:cateIDs IS NULL OR :cateIDs = '') OR products.category_id IN (:cateIDs)) " +
            "AND ((:exp IS NULL OR :exp = '') OR sm.expiry = :exp) " +
            "AND ((:tran IS NULL OR :tran = '') OR (transaction = :tran AND transaction_id != :tranId)) " +
            "GROUP BY sm.product_id, sm.expiry " +
            "ORDER BY products.product_code, sm.expiry ASC ",
            nativeQuery = true)
    List<StockMovementEntity> findProductsExpiries(Long whId, Long pId, String[] bndIds, String[] cateIDs, LocalDate exp, String tran, Long tranId);

    @Query(value = """
            SELECT p.product_id AS productId, COALESCE(sm.expiry, null) AS expiry, COALESCE(SUM(sm.quantity), 0) AS quantity FROM products p
            LEFT JOIN stock_movement sm ON p.product_id = sm.product_id AND sm.warehouse_id = :whId
            WHERE
                p.status = 1 AND p.type = 'standard' AND
                ((:pId IS NULL OR :pId = '') OR p.product_id = :pId) AND
                (
                    ((:bndIds IS NULL OR :bndIds = '') OR p.brand_id IN (:bndIds)) OR
                    ((:cateIDs IS NULL OR :cateIDs = '') OR p.category_id IN (:cateIDs))
                ) AND
                ((:exp IS NULL OR :exp = '') OR sm.expiry = :exp)
            GROUP BY p.product_id, sm.expiry
            ORDER BY p.product_code, sm.expiry ASC
            """,
            nativeQuery = true)
    List<StockCountItem> findProductsExpiriesCount(Long whId, Long pId, String[] bndIds, String[] cateIDs, LocalDate exp);

    @Query(value = "SELECT COALESCE(avg_cost, 0) FROM stock_balance WHERE product_id = :pId LIMIT 1", nativeQuery = true)
    Double findProdAvgCost(Long pId);

    @Query(value = """
            SELECT id, date, transaction, transaction_id, warehouse_id, product_id, unit_id, unit_quantity, SUM(quantity) AS quantity, expiry, cost
            FROM stock_movement
            WHERE
                ((:whIds IS NULL OR :whIds = '') OR FIND_IN_SET(warehouse_id, :whIds)) AND
                ((:whId IS NULL OR :whId = '') OR warehouse_id = :whId) AND
                product_id = :pId
            GROUP BY product_id, expiry
            ORDER BY expiry DESC
            """,
            nativeQuery = true)
    List<StockMovementEntity> findStockExpiries(Long pId, String whIds, Long whId);

    @Query(value = """
        SELECT
            COALESCE(
            (
                CASE
                    WHEN (:whIds IS NOT NULL AND TRIM(:whIds) <> '') OR (:whId IS NOT NULL AND TRIM(:whId) <> '') THEN
                    (
                        SELECT COALESCE(SUM(quantity), 0) AS quantity
                        FROM stock_movement
                        WHERE
                            ((:whIds IS NULL OR :whIds = '') OR FIND_IN_SET(warehouse_id, :whIds)) AND
                            ((:whId IS NULL OR :whId = '') OR warehouse_id = :whId) AND
                            product_id = :pId
                        LIMIT 1
                    )
                    ELSE
                    (
                        SELECT COALESCE(quantity_available, 0) AS quantity
                        FROM stock_balance
                        WHERE product_id = :pId
                        LIMIT 1
                    )
                END
            ), 0)
        FROM dual
        """,
        nativeQuery = true)
    double findStockBalance(Long pId, String whIds, Long whId);
}
