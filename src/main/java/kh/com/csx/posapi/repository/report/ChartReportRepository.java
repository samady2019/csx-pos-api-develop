package kh.com.csx.posapi.repository.report;

import jakarta.persistence.Tuple;
import kh.com.csx.posapi.dto.report.chartReport.*;
import kh.com.csx.posapi.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChartReportRepository extends JpaRepository<PaymentEntity, Long> {
    @Query(value = """
        SELECT
            period.month AS month,
            period.year AS year
        FROM (
            SELECT
                sale.month AS month,
                sale.year AS year
            FROM (
                SELECT
                    DATE_FORMAT(s.date, '%m') AS month,
                    DATE_FORMAT(s.date, '%Y') AS year
                FROM SaleEntity s
                WHERE
                    s.status != 'pending' AND
                    (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR s.createdBy = :#{#filter.user}) AND
                    (:#{#filter.bIds} IS NULL OR s.billerId IN :#{#filter.bIds}) AND
                    (:#{#filter.whIds} IS NULL OR s.warehouseId IN :#{#filter.whIds}) AND
                    (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR s.billerId = :#{#filter.billerId}) AND
                    (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR s.warehouseId = :#{#filter.warehouseId}) AND
                    (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR s.createdBy = :#{#filter.createdBy}) AND
                    (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR s.updatedBy = :#{#filter.updatedBy})
                GROUP BY YEAR(s.date), MONTH(s.date)
                ORDER BY s.date DESC
                LIMIT 12
            ) sale
            UNION ALL
            SELECT
                purchase.month AS month,
                purchase.year AS year
            FROM (
                SELECT
                    DATE_FORMAT(p.date, '%m') AS month,
                    DATE_FORMAT(p.date, '%Y') AS year
                FROM PurchaseEntity p
                WHERE
                    p.status != 'pending' AND
                    (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR p.createdBy = :#{#filter.user}) AND
                    (:#{#filter.bIds} IS NULL OR p.billerId IN :#{#filter.bIds}) AND
                    (:#{#filter.whIds} IS NULL OR p.warehouseId IN :#{#filter.whIds}) AND
                    (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR p.billerId = :#{#filter.billerId}) AND
                    (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR p.warehouseId = :#{#filter.warehouseId}) AND
                    (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR p.createdBy = :#{#filter.createdBy}) AND
                    (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR p.updatedBy = :#{#filter.updatedBy})
                GROUP BY YEAR(p.date), MONTH(p.date)
                ORDER BY p.date DESC
                LIMIT 12
            ) purchase
        ) period
        GROUP BY period.year, period.month
        ORDER BY period.year DESC, period.month DESC
        LIMIT 12
    """)
    List<Tuple> findPeriodOverview(@Param("filter") OverviewRequest filter);

    @Query(value = """
        SELECT
            COALESCE(SUM(s.total), 0) AS total,
            COALESCE(SUM(s.orderDiscount), 0) AS discount,
            COALESCE(SUM(s.productTax), 0) AS productTax,
            COALESCE(SUM(s.orderTax), 0) AS orderTax,
            COALESCE(SUM(s.shipping), 0) AS shipping,
            COALESCE(SUM(s.grandTotal), 0) AS grandTotal
        FROM SaleEntity s
        WHERE
            s.status != 'pending' AND
            (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR s.createdBy = :#{#filter.user}) AND
            (:#{#filter.bIds} IS NULL OR s.billerId IN :#{#filter.bIds}) AND
            (:#{#filter.whIds} IS NULL OR s.warehouseId IN :#{#filter.whIds}) AND
            (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR s.billerId = :#{#filter.billerId}) AND
            (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR s.warehouseId = :#{#filter.warehouseId}) AND
            (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR s.createdBy = :#{#filter.createdBy}) AND
            (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR s.updatedBy = :#{#filter.updatedBy}) AND
            (YEAR(s.date) = :year AND MONTH(s.date) = :month)
    """)
    Tuple findSaleByPeriod(@Param("filter") OverviewRequest filter, String year, String month);

    @Query(value = """
        SELECT
            COALESCE(SUM(p.total), 0) AS total,
            COALESCE(SUM(p.orderDiscount), 0) AS discount,
            COALESCE(SUM(p.productTax), 0) AS productTax,
            COALESCE(SUM(p.orderTax), 0) AS orderTax,
            COALESCE(SUM(p.shipping), 0) AS shipping,
            COALESCE(SUM(p.grandTotal), 0) AS grandTotal
        FROM PurchaseEntity p
        WHERE
            p.status != 'pending' AND
            (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR p.createdBy = :#{#filter.user}) AND
            (:#{#filter.bIds} IS NULL OR p.billerId IN :#{#filter.bIds}) AND
            (:#{#filter.whIds} IS NULL OR p.warehouseId IN :#{#filter.whIds}) AND
            (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR p.billerId = :#{#filter.billerId}) AND
            (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR p.warehouseId = :#{#filter.warehouseId}) AND
            (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR p.createdBy = :#{#filter.createdBy}) AND
            (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR p.updatedBy = :#{#filter.updatedBy}) AND
            (YEAR(p.date) = :year AND MONTH(p.date) = :month)
    """)
    Tuple findPurchaseByPeriod(@Param("filter") OverviewRequest filter, String year, String month);

    @Query(value = """
        SELECT
            COUNT(p.productId) AS totalItems,
            ROUND(SUM(COALESCE(wp.quantity, 0)), 2) AS totalQuantity,
            ROUND(SUM(COALESCE(wp.quantity, 0) * COALESCE(s.avgCost, 0)), 2) AS stockValueByCost,
            ROUND(SUM(COALESCE(wp.quantity, 0) * COALESCE(u.baseUnitPrice, 0)), 2) AS stockValueByPrice,
            ROUND((
                SUM(COALESCE(wp.quantity, 0) * COALESCE(u.baseUnitPrice, 0)) -
                SUM(COALESCE(wp.quantity, 0) * COALESCE(s.avgCost, 0))
            ), 2) AS profitEstimate
        FROM ProductEntity p
        LEFT JOIN (
            SELECT
                _u.unitId AS unitId, _u.unitCode AS unitCode, _u.unitNameEn AS unitNameEn, _u.unitNameKh AS unitNameKh,
                _pu.id.productId AS productId,
                _pu.cost AS baseUnitCost,
                _pu.price AS baseUnitPrice
            FROM UnitEntity _u
            INNER JOIN ProductUnitEntity _pu ON _pu.id.unitId = _u.unitId AND _u.punitId IS NULL
        ) u ON u.productId = p.productId
        LEFT JOIN StockBalanceEntity s ON s.productId = p.productId
        LEFT JOIN (
            SELECT
                _p.productId AS productId,
                COALESCE(SUM(_wp.quantity), 0) AS quantity
            FROM ProductEntity _p
            LEFT JOIN WarehouseProductEntity _wp ON _wp.productId = _p.productId
            WHERE
                _p.status = 1 AND _p.type = 'standard' AND
                (:#{#filter.whIds} IS NULL OR _wp.warehouseId IN :#{#filter.whIds}) AND
                (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR _wp.warehouseId = :#{#filter.warehouseId})
            GROUP BY _p.productId
        ) wp ON wp.productId = p.productId
        WHERE p.status = 1 AND p.type = 'standard'
    """)
    Tuple findStockValue(@Param("filter") OverviewRequest filter);

    @Query(value = """
        SELECT
            c.categoryId AS categoryId,
            c.code AS code,
            c.name AS name,
            ROUND(SUM(COALESCE(wp.quantity, 0)), 2) AS totalQuantity
        FROM ProductEntity p
        INNER JOIN CategoryEntity c ON c.categoryId = p.categoryId
        LEFT JOIN (
            SELECT
                _p.productId AS productId,
                COALESCE(SUM(_wp.quantity), 0) AS quantity
            FROM ProductEntity _p
            LEFT JOIN WarehouseProductEntity _wp ON _wp.productId = _p.productId
            WHERE
                _p.status = 1 AND _p.type = 'standard' AND
                (:#{#filter.whIds} IS NULL OR _wp.warehouseId IN :#{#filter.whIds}) AND
                (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR _wp.warehouseId = :#{#filter.warehouseId})
            GROUP BY _p.productId
        ) wp ON wp.productId = p.productId
        WHERE
            p.status = 1 AND p.type = 'standard' AND
            c.status = 1
        GROUP BY c.categoryId
        HAVING SUM(COALESCE(wp.quantity, 0)) > 0
        ORDER BY c.code ASC
    """)
    List<Tuple> findStockCategories(@Param("filter") OverviewRequest filter);

    @Query(value = """
        SELECT
            w.id AS id,
            w.code AS code,
            w.name AS name,
            ROUND(COALESCE(s.quantity, 0), 2) AS totalQuantity
        FROM WarehouseEntity w
        LEFT JOIN (
            SELECT
                _wp.warehouseId AS warehouseId,
                SUM(COALESCE(_wp.quantity, 0)) AS quantity
            FROM ProductEntity _p
            INNER JOIN WarehouseProductEntity _wp ON _wp.productId = _p.productId
            WHERE _p.status = 1 AND _p.type = 'standard'
            GROUP BY _wp.warehouseId
        ) s ON s.warehouseId = w.id
        WHERE
            (:#{#filter.whIds} IS NULL OR w.id IN :#{#filter.whIds}) AND
            (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR w.id = :#{#filter.warehouseId})
        ORDER BY w.id ASC
    """)
    List<Tuple> findStockWarehouses(@Param("filter") OverviewRequest filter);

    @Query(value = """
        SELECT
            COALESCE(:#{#filter.month}, '') AS month,
            COALESCE(:#{#filter.year}, '') AS year,
            p.productId AS productId,
            p.productCode AS productCode,
            p.barCode AS barCode,
            p.productNameEn AS productNameEn,
            p.productNameKh AS productNameKh,
            u.unitCode AS unitCode,
            u.unitNameEn AS unitNameEn,
            u.unitNameKh AS unitNameKh,
            COALESCE(s.quantity, 0) AS totalQuantity
        FROM ProductEntity p
        LEFT JOIN (
            SELECT
                _u.unitId AS unitId, _u.unitCode AS unitCode, _u.unitNameEn AS unitNameEn, _u.unitNameKh AS unitNameKh, _pu.id.productId AS productId
            FROM UnitEntity _u
            INNER JOIN ProductUnitEntity _pu ON _pu.id.unitId = _u.unitId AND _u.punitId IS NULL
        ) u ON u.productId = p.productId
        LEFT JOIN (
            SELECT
                _si.productId AS productId,
                COALESCE(SUM(_si.quantity), 0) AS quantity
            FROM SaleEntity _s
            INNER JOIN SaleItemEntity _si ON _si.saleId = _s.id
            WHERE
                _s.status != 'pending' AND
                (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR _s.createdBy = :#{#filter.user}) AND
                (:#{#filter.bIds} IS NULL OR _s.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.whIds} IS NULL OR _s.warehouseId IN :#{#filter.whIds}) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR _s.billerId = :#{#filter.billerId}) AND
                (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR _s.warehouseId = :#{#filter.warehouseId}) AND
                (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR _s.createdBy = :#{#filter.createdBy}) AND
                (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR _s.updatedBy = :#{#filter.updatedBy}) AND
                (YEAR(_s.date) = :#{#filter.year} AND MONTH(_s.date) = :#{#filter.month})
            GROUP BY _si.productId
        ) s ON s.productId = p.productId
        WHERE p.status = 1 AND (s.quantity IS NOT NULL AND s.quantity > 0)
        ORDER BY s.quantity DESC
        LIMIT 10
    """)
    List<Tuple> findBestSellers(@Param("filter") OverviewRequest filter);

    @Query(value = """
        SELECT
            COALESCE(:#{#filter.month}, '') AS month,
            COALESCE(:#{#filter.year}, '') AS year,
            xpc.id AS id,
            xpc.code AS code,
            xpc.name AS name,
            COALESCE(xp.total, 0) AS total
        FROM ExpenseCategoryEntity xpc
        LEFT JOIN (
            SELECT
                _xp.expenseCategoryId AS expenseCategoryId,
                COALESCE(SUM(_xp.amount), 0) as total
            FROM ExpenseEntity _xp
            WHERE
                (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR _xp.createdBy = :#{#filter.user}) AND
                (:#{#filter.bIds} IS NULL OR _xp.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.billerId} IS NULL OR _xp.billerId = :#{#filter.billerId}) AND
                (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR _xp.createdBy = :#{#filter.createdBy}) AND
                (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR _xp.updatedBy = :#{#filter.updatedBy}) AND
                (YEAR(_xp.date) = :#{#filter.year} AND MONTH(_xp.date) = :#{#filter.month})
            GROUP BY _xp.expenseCategoryId
        ) xp ON xp.expenseCategoryId = xpc.id
    """)
    List<Tuple> findExpenseCategories(@Param("filter") OverviewRequest filter);
}
