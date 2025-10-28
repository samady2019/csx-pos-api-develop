package kh.com.csx.posapi.repository.report;

import jakarta.persistence.Tuple;
import kh.com.csx.posapi.dto.report.inventoryReport.*;
import kh.com.csx.posapi.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface InventoryReportRepository extends JpaRepository<ProductEntity, Long> {
    @Query(value = """
        SELECT
            p.productId AS productId,
            p.productCode AS productCode,
            p.barCode AS barCode,
            p.productNameEn AS productNameEn,
            p.productNameKh AS productNameKh,
            b.name AS brand,
            c.name AS category,
            u.unitCode AS unitCode,
            u.unitNameEn AS unitNameEn,
            u.unitNameKh AS unitNameKh,
            COALESCE(wp.quantity, 0) AS quantity,
            COALESCE(p.alertQuantity, 0) AS alertQuantity,
            wp.warehouseIds AS warehouseIds
        FROM ProductEntity p
        LEFT JOIN BrandEntity b ON b.brandId = p.brandId
        LEFT JOIN CategoryEntity c ON c.categoryId = p.categoryId
        LEFT JOIN (
            SELECT
                _u.unitId AS unitId, _u.unitCode AS unitCode, _u.unitNameEn AS unitNameEn, _u.unitNameKh AS unitNameKh, _pu.id.productId AS productId
            FROM UnitEntity _u
            INNER JOIN ProductUnitEntity _pu ON _pu.id.unitId = _u.unitId AND _u.punitId IS NULL
        ) u ON u.productId = p.productId
        LEFT JOIN (
            SELECT _p.productId AS productId, GROUP_CONCAT(_w.id) AS warehouseIds, COALESCE(SUM(_wp.quantity), 0) AS quantity
            FROM ProductEntity _p
            INNER JOIN WarehouseEntity _w ON (:#{#filter.whIds} IS NULL OR _w.id IN :#{#filter.whIds})
            LEFT JOIN WarehouseProductEntity _wp ON _wp.warehouseId = _w.id AND _wp.productId = _p.productId
            WHERE
                (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR _p.productId = :#{#filter.productId}) AND
                (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR _w.id = :#{#filter.warehouseId})
            GROUP BY _p.productId
        ) wp ON wp.productId = p.productId
        WHERE
            p.status = 1 AND p.type = 'standard' AND
            (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR :#{#filter.warehouseId} IN (wp.warehouseIds)) AND
            (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR p.productId = :#{#filter.productId}) AND
            (:#{#filter.productCode} IS NULL OR :#{#filter.productCode} = '' OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :#{#filter.productCode}, '%'))) AND
            (:#{#filter.barCode} IS NULL OR :#{#filter.barCode} = '' OR LOWER(p.barCode) LIKE LOWER(CONCAT('%', :#{#filter.barCode}, '%'))) AND
            (:#{#filter.productNameEn} IS NULL OR :#{#filter.productNameEn} = '' OR LOWER(p.productNameEn) LIKE LOWER(CONCAT('%', :#{#filter.productNameEn}, '%'))) AND
            (:#{#filter.productNameKh} IS NULL OR :#{#filter.productNameKh} = '' OR LOWER(p.productNameKh) LIKE LOWER(CONCAT('%', :#{#filter.productNameKh}, '%'))) AND
            (:#{#filter.brandId} IS NULL OR :#{#filter.brandId} = '' OR p.brandId = :#{#filter.brandId}) AND
            (:#{#filter.categoryId} IS NULL OR :#{#filter.categoryId} = '' OR p.categoryId = :#{#filter.categoryId}) AND
            COALESCE(wp.quantity, 0) <= COALESCE(p.alertQuantity, 0) AND
            (
                (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') OR
                (
                    LOWER(p.productCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(p.barCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(p.productNameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(p.productNameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(b.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(c.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(u.unitCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(u.unitNameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(u.unitNameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                )
            )
        GROUP BY p.productId
    """)
    Page<Tuple> findProductQuantityAlerts(@Param("filter") ProductRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            p.productId AS productId,
            p.productCode AS productCode,
            p.barCode AS barCode,
            p.productNameEn AS productNameEn,
            p.productNameKh AS productNameKh,
            b.name AS brand,
            c.name AS category,
            u.unitCode AS unitCode,
            u.unitNameEn AS unitNameEn,
            u.unitNameKh AS unitNameKh,
            sm.expiry AS expiry,
            COALESCE(sm.quantity, 0) AS quantity
        FROM ProductEntity p
        LEFT JOIN BrandEntity b ON b.brandId = p.brandId
        LEFT JOIN CategoryEntity c ON c.categoryId = p.categoryId
        LEFT JOIN (
            SELECT
                _u.unitId AS unitId, _u.unitCode AS unitCode, _u.unitNameEn AS unitNameEn, _u.unitNameKh AS unitNameKh, _pu.id.productId AS productId
            FROM UnitEntity _u
            INNER JOIN ProductUnitEntity _pu ON _pu.id.unitId = _u.unitId AND _u.punitId IS NULL
        ) u ON u.productId = p.productId
        INNER JOIN (
            SELECT _sm.productId AS productId, GROUP_CONCAT(_sm.warehouseId) AS warehouseIds, COALESCE(SUM(_sm.quantity), 0) AS quantity, _sm.expiry AS expiry
            FROM StockMovementEntity _sm
            INNER JOIN ProductEntity _p ON _p.productId = _sm.productId
            INNER JOIN SettingEntity _s ON 1=1
            WHERE
                (:#{#filter.whIds} IS NULL OR _sm.warehouseId IN :#{#filter.whIds}) AND
                (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR _sm.warehouseId = :#{#filter.warehouseId}) AND
                (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR _sm.productId = :#{#filter.productId}) AND
                (_sm.expiry IS NOT NULL) AND
                CURRENT_DATE >= DATEADD(DAY, -COALESCE(_p.expiryAlertDays, _s.expiryAlertDays), _sm.expiry)
            GROUP BY _sm.productId, _sm.expiry
        ) sm ON sm.productId = p.productId
        WHERE
            p.status = 1 AND p.type = 'standard' AND
            (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR p.productId = :#{#filter.productId}) AND
            (:#{#filter.productCode} IS NULL OR :#{#filter.productCode} = '' OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :#{#filter.productCode}, '%'))) AND
            (:#{#filter.barCode} IS NULL OR :#{#filter.barCode} = '' OR LOWER(p.barCode) LIKE LOWER(CONCAT('%', :#{#filter.barCode}, '%'))) AND
            (:#{#filter.productNameEn} IS NULL OR :#{#filter.productNameEn} = '' OR LOWER(p.productNameEn) LIKE LOWER(CONCAT('%', :#{#filter.productNameEn}, '%'))) AND
            (:#{#filter.productNameKh} IS NULL OR :#{#filter.productNameKh} = '' OR LOWER(p.productNameKh) LIKE LOWER(CONCAT('%', :#{#filter.productNameKh}, '%'))) AND
            (:#{#filter.brandId} IS NULL OR :#{#filter.brandId} = '' OR p.brandId = :#{#filter.brandId}) AND
            (:#{#filter.categoryId} IS NULL OR :#{#filter.categoryId} = '' OR p.categoryId = :#{#filter.categoryId}) AND
            COALESCE(sm.quantity, 0) > 0 AND
            (
                (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') OR
                (
                    LOWER(p.productCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(p.barCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(p.productNameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(p.productNameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(b.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(c.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(u.unitCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(u.unitNameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(u.unitNameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(FUNCTION('DATE_FORMAT', sm.expiry, dFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                )
            )
    """)
    Page<Tuple> findProductExpiryAlerts(@Param("filter") ProductRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            c.categoryId AS categoryId,
            c.code AS code,
            c.name AS name,
            COALESCE(p.quantity, 0) AS purchaseQuantity,
            COALESCE(p.amount, 0) AS purchaseAmount,
            COALESCE(s.quantity, 0) AS soldQuantity,
            COALESCE(s.amount, 0) AS soldAmount
        FROM CategoryEntity c
        LEFT JOIN (
            SELECT _c.categoryId AS categoryId, COALESCE(SUM(_pi.quantity), 0) AS quantity, COALESCE(SUM(_pi.subtotal), 0) AS amount
            FROM PurchaseEntity _p
            INNER JOIN PurchaseItemEntity _pi ON _pi.purchaseId = _p.id
            INNER JOIN ProductEntity _prd ON _prd.productId = _pi.productId
            INNER JOIN CategoryEntity _c ON _c.categoryId = _prd.categoryId
            WHERE _p.status != 'pending' AND _c.status = 1 AND
                (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR _p.createdBy = :#{#filter.user}) AND
                (:#{#filter.bIds} IS NULL OR _p.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.whIds} IS NULL OR _p.warehouseId IN :#{#filter.whIds}) AND
                (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (_p.date BETWEEN :#{#filter.start} AND :#{#filter.end})) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR _p.billerId = :#{#filter.billerId}) AND
                (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR _p.warehouseId = :#{#filter.warehouseId}) AND
                (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR _p.createdBy = :#{#filter.createdBy})
            GROUP BY _c.categoryId
        ) p ON p.categoryId = c.categoryId
        LEFT JOIN (
            SELECT _c.categoryId AS categoryId, COALESCE(SUM(_si.quantity), 0) AS quantity, COALESCE(SUM(_si.subtotal), 0) AS amount
            FROM SaleEntity _s
            INNER JOIN SaleItemEntity _si ON _si.saleId = _s.id
            INNER JOIN ProductEntity _prd ON _prd.productId = _si.productId
            INNER JOIN CategoryEntity _c ON _c.categoryId = _prd.categoryId
            WHERE _s.status != 'pending' AND _c.status = 1 AND
                (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR _s.createdBy = :#{#filter.user}) AND
                (:#{#filter.bIds} IS NULL OR _s.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.whIds} IS NULL OR _s.warehouseId IN :#{#filter.whIds}) AND
                (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (_s.date BETWEEN :#{#filter.start} AND :#{#filter.end})) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR _s.billerId = :#{#filter.billerId}) AND
                (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR _s.warehouseId = :#{#filter.warehouseId}) AND
                (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR _s.createdBy = :#{#filter.createdBy})
            GROUP BY _c.categoryId
        ) s ON s.categoryId = c.categoryId
        WHERE c.status = 1 AND
            (:#{#filter.categoryId} IS NULL OR :#{#filter.categoryId} = '' OR c.categoryId = :#{#filter.categoryId}) AND
            (:#{#filter.code} IS NULL OR :#{#filter.code} = '' OR LOWER(c.code) LIKE LOWER(CONCAT('%', :#{#filter.code}, '%'))) AND
            (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%'))) AND
            (
                (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') OR
                (
                    LOWER(c.code) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(c.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                )
            )
    """)
    Page<Tuple> findCategories(@Param("filter") CategoryRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            b.brandId AS brandId,
            b.code AS code,
            b.name AS name,
            COALESCE(p.quantity, 0) AS purchaseQuantity,
            COALESCE(p.amount, 0) AS purchaseAmount,
            COALESCE(s.quantity, 0) AS soldQuantity,
            COALESCE(s.amount, 0) AS soldAmount
        FROM BrandEntity b
        LEFT JOIN (
            SELECT _b.brandId AS brandId, COALESCE(SUM(_pi.quantity), 0) AS quantity, COALESCE(SUM(_pi.subtotal), 0) AS amount
            FROM PurchaseEntity _p
            INNER JOIN PurchaseItemEntity _pi ON _pi.purchaseId = _p.id
            INNER JOIN ProductEntity _prd ON _prd.productId = _pi.productId
            INNER JOIN BrandEntity _b ON _b.brandId = _prd.brandId
            WHERE _p.status != 'pending' AND _b.status = 1 AND
                (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR _p.createdBy = :#{#filter.user}) AND
                (:#{#filter.bIds} IS NULL OR _p.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.whIds} IS NULL OR _p.warehouseId IN :#{#filter.whIds}) AND
                (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (_p.date BETWEEN :#{#filter.start} AND :#{#filter.end})) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR _p.billerId = :#{#filter.billerId}) AND
                (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR _p.warehouseId = :#{#filter.warehouseId}) AND
                (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR _p.createdBy = :#{#filter.createdBy})
            GROUP BY _b.brandId
        ) p ON p.brandId = b.brandId
        LEFT JOIN (
            SELECT _b.brandId AS brandId, COALESCE(SUM(_si.quantity), 0) AS quantity, COALESCE(SUM(_si.subtotal), 0) AS amount
            FROM SaleEntity _s
            INNER JOIN SaleItemEntity _si ON _si.saleId = _s.id
            INNER JOIN ProductEntity _prd ON _prd.productId = _si.productId
            INNER JOIN BrandEntity _b ON _b.brandId = _prd.brandId
            WHERE _s.status != 'pending' AND _b.status = 1 AND
                (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR _s.createdBy = :#{#filter.user}) AND
                (:#{#filter.bIds} IS NULL OR _s.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.whIds} IS NULL OR _s.warehouseId IN :#{#filter.whIds}) AND
                (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (_s.date BETWEEN :#{#filter.start} AND :#{#filter.end})) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR _s.billerId = :#{#filter.billerId}) AND
                (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR _s.warehouseId = :#{#filter.warehouseId}) AND
                (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR _s.createdBy = :#{#filter.createdBy})
            GROUP BY _b.brandId
        ) s ON s.brandId = b.brandId
        WHERE b.status = 1 AND
            (:#{#filter.brandId} IS NULL OR :#{#filter.brandId} = '' OR b.brandId = :#{#filter.brandId}) AND
            (:#{#filter.code} IS NULL OR :#{#filter.code} = '' OR LOWER(b.code) LIKE LOWER(CONCAT('%', :#{#filter.code}, '%'))) AND
            (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%'))) AND
            (
                (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') OR
                (
                    LOWER(b.code) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(b.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                )
            )
    """)
    Page<Tuple> findBrands(@Param("filter") BrandRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            p.productId AS productId,
            p.productCode AS productCode,
            p.barCode AS barCode,
            p.productNameEn AS productNameEn,
            p.productNameKh AS productNameKh,
            b.name AS brand,
            c.name AS category,
            u.unitCode AS unitCode,
            u.unitNameEn AS unitNameEn,
            u.unitNameKh AS unitNameKh,
            sm.expiry AS expiry
        FROM ProductEntity p
        LEFT JOIN BrandEntity b ON b.brandId = p.brandId
        LEFT JOIN CategoryEntity c ON c.categoryId = p.categoryId
        LEFT JOIN (
            SELECT
                _u.unitId AS unitId, _u.unitCode AS unitCode, _u.unitNameEn AS unitNameEn, _u.unitNameKh AS unitNameKh, _pu.id.productId AS productId
            FROM UnitEntity _u
            INNER JOIN ProductUnitEntity _pu ON _pu.id.unitId = _u.unitId AND _u.punitId IS NULL
        ) u ON u.productId = p.productId
        LEFT JOIN (
            SELECT _sm.productId AS productId, _sm.expiry AS expiry
            FROM (
                SELECT
                    __sm.productId AS productId,
                    __sm.warehouseId AS warehouseId,
                    __sm.expiry AS expiry,
                    COALESCE(SUM(__sm.quantity), 0) AS quantity
                FROM StockMovementEntity __sm
                WHERE
                    (:#{#filter.whIds} IS NULL OR __sm.warehouseId IN :#{#filter.whIds}) AND
                    (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR __sm.warehouseId = :#{#filter.warehouseId}) AND
                    (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR __sm.productId = :#{#filter.productId})
                GROUP BY __sm.warehouseId, __sm.productId, __sm.expiry
                HAVING COALESCE(SUM(__sm.quantity), 0) > 0
            ) _sm
            GROUP BY _sm.productId, _sm.expiry
        ) sm ON sm.productId = p.productId
        WHERE
            p.status = 1 AND p.type = 'standard' AND
            (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR p.productId = :#{#filter.productId}) AND
            (:#{#filter.productCode} IS NULL OR :#{#filter.productCode} = '' OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :#{#filter.productCode}, '%'))) AND
            (:#{#filter.barCode} IS NULL OR :#{#filter.barCode} = '' OR LOWER(p.barCode) LIKE LOWER(CONCAT('%', :#{#filter.barCode}, '%'))) AND
            (:#{#filter.productNameEn} IS NULL OR :#{#filter.productNameEn} = '' OR LOWER(p.productNameEn) LIKE LOWER(CONCAT('%', :#{#filter.productNameEn}, '%'))) AND
            (:#{#filter.productNameKh} IS NULL OR :#{#filter.productNameKh} = '' OR LOWER(p.productNameKh) LIKE LOWER(CONCAT('%', :#{#filter.productNameKh}, '%'))) AND
            (:#{#filter.brandId} IS NULL OR :#{#filter.brandId} = '' OR p.brandId = :#{#filter.brandId}) AND
            (:#{#filter.categoryId} IS NULL OR :#{#filter.categoryId} = '' OR p.categoryId = :#{#filter.categoryId}) AND
            (
                (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') OR
                (
                    LOWER(p.productCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(p.barCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(p.productNameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(p.productNameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(b.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(c.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(u.unitCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(u.unitNameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(u.unitNameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(FUNCTION('DATE_FORMAT', sm.expiry, dFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                )
            )
    """)
    Page<Tuple> findProductsExpires(@Param("filter") ProductRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            w.id AS warehouseId,
            w.code AS warehouseCode,
            w.name AS warehouseName,
            p.productId AS productId,
            :exp AS expiry,
            COALESCE(SUM(sm.quantity), 0) AS quantity
        FROM ProductEntity p
        LEFT JOIN WarehouseEntity w ON w.id = :whId
        LEFT JOIN StockMovementEntity sm ON sm.warehouseId = w.id AND sm.productId = p.productId AND ((:exp IS NOT NULL AND sm.expiry = :exp) OR (:exp IS NULL AND sm.expiry IS NULL))
        WHERE p.productId = :pId AND w.id = :whId
        GROUP BY sm.productId
    """)
    Tuple findProductExp(Long whId, Long pId, LocalDate exp);

    @Query(value = """
        SELECT
            aj.id AS id,
            aj.countId AS countId,
            aj.date AS date,
            aj.referenceNo AS referenceNo,
            CONCAT(b.companyEn, ' (', b.code, ')') AS biller,
            w.name AS warehouse,
            aj.attachment AS attachment,
            aj.note AS note,
            COALESCE(CONCAT(e.firstName, ' ', e.lastName), u.username) AS createdBy
        FROM AdjustmentEntity aj
        INNER JOIN (
            SELECT _aji.adjustment.id AS adjustmentId
            FROM AdjustmentItemEntity _aji
            WHERE (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR _aji.productId = :#{#filter.productId})
            GROUP BY _aji.adjustment.id
        ) aji ON aji.adjustmentId = aj.id
        LEFT JOIN BillerEntity b ON b.id = aj.billerId
        LEFT JOIN WarehouseEntity w ON w.id = aj.warehouseId
        LEFT JOIN UserEntity u ON u.userId = aj.createdBy
        LEFT JOIN EmployeeEntity e ON e.id = u.employeeId
        WHERE
             (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR aj.createdBy = :#{#filter.user}) AND
             (:#{#filter.bIds} IS NULL OR aj.billerId IN :#{#filter.bIds}) AND
             (:#{#filter.whIds} IS NULL OR aj.warehouseId IN :#{#filter.whIds}) AND
             (:#{#filter.id} IS NULL OR aj.id = :#{#filter.id}) AND
             (:#{#filter.countId} IS NULL OR aj.countId = :#{#filter.countId}) AND
             (:#{#filter.referenceNo} IS NULL OR LOWER(aj.referenceNo) LIKE LOWER(CONCAT('%', :#{#filter.referenceNo}, '%'))) AND
             (:#{#filter.billerId} IS NULL OR aj.billerId = :#{#filter.billerId}) AND
             (:#{#filter.warehouseId} IS NULL OR aj.warehouseId = :#{#filter.warehouseId}) AND
             (:#{#filter.createdBy} IS NULL OR aj.createdBy = :#{#filter.createdBy}) AND
             (:#{#filter.updatedBy} IS NULL OR aj.updatedBy = :#{#filter.updatedBy}) AND
             (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (aj.date BETWEEN :#{#filter.start} AND :#{#filter.end})) AND
             (
                (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') OR
                (
                    LOWER(FUNCTION('DATE_FORMAT', aj.date, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(aj.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CONCAT(b.companyEn, ' (', b.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CONCAT(w.name, ' (', w.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(aj.note) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CASE WHEN e.id IS NULL THEN u.username ELSE CONCAT(COALESCE(e.firstName, ''), ' ', COALESCE(e.lastName, '')) END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                )
             )
    """)
    Page<Tuple> findAdjustments(@Param("filter") AdjustmentRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            tr.id AS id,
            tr.date AS date,
            tr.referenceNo AS referenceNo,
            CONCAT(fb.companyEn, ' (', fb.code, ')') AS fromBiller,
            CONCAT(tb.companyEn, ' (', tb.code, ')') AS toBiller,
            fw.name AS fromWarehouse,
            tw.name AS toWarehouse,
            tr.attachment AS attachment,
            tr.note AS note,
            tr.status AS status,
            COALESCE(CONCAT(e.firstName, ' ', e.lastName), u.username) AS createdBy
        FROM TransferEntity tr
        INNER JOIN (
            SELECT _tri.transferId AS transferId
            FROM TransferItemEntity _tri
            WHERE (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR _tri.productId = :#{#filter.productId})
            GROUP BY _tri.transferId
        ) tri ON tri.transferId = tr.id
        LEFT JOIN BillerEntity fb ON fb.id = tr.fromBiller
        LEFT JOIN BillerEntity tb ON tb.id = tr.toBiller
        LEFT JOIN WarehouseEntity fw ON fw.id = tr.fromWarehouse
        LEFT JOIN WarehouseEntity tw ON tw.id = tr.toWarehouse
        LEFT JOIN UserEntity u ON u.userId = tr.createdBy
        LEFT JOIN EmployeeEntity e ON e.id = u.employeeId
        WHERE
             (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR tr.createdBy = :#{#filter.user}) AND
             (:#{#filter.bIds} IS NULL OR (tr.fromBiller IN :#{#filter.bIds} OR tr.toBiller IN :#{#filter.bIds})) AND
             (:#{#filter.whIds} IS NULL OR (tr.fromWarehouse IN :#{#filter.whIds} OR tr.toWarehouse IN :#{#filter.whIds})) AND
             (:#{#filter.id} IS NULL OR tr.id = :#{#filter.id}) AND
             (:#{#filter.referenceNo} IS NULL OR LOWER(tr.referenceNo) LIKE LOWER(CONCAT('%', :#{#filter.referenceNo}, '%'))) AND
             (:#{#filter.billerId} IS NULL OR (tr.fromBiller = :#{#filter.billerId} OR tr.toBiller = :#{#filter.billerId})) AND
             (:#{#filter.warehouseId} IS NULL OR (tr.fromWarehouse = :#{#filter.warehouseId} OR tr.toWarehouse = :#{#filter.warehouseId})) AND
             (:#{#filter.status} IS NULL OR :#{#filter.status} = '' OR tr.status = :#{#filter.status}) AND
             (:#{#filter.createdBy} IS NULL OR tr.createdBy = :#{#filter.createdBy}) AND
             (:#{#filter.updatedBy} IS NULL OR tr.updatedBy = :#{#filter.updatedBy}) AND
             (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (tr.date BETWEEN :#{#filter.start} AND :#{#filter.end})) AND
             (
                (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') OR
                (
                    LOWER(FUNCTION('DATE_FORMAT', tr.date, dtFormatMySQL())) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(tr.referenceNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CONCAT(fb.companyEn, ' (', fb.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CONCAT(tb.companyEn, ' (', tb.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CONCAT(fw.name, ' (', fw.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CONCAT(tw.name, ' (', tw.code, ')')) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(tr.status) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(tr.note) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CASE WHEN e.id IS NULL THEN u.username ELSE CONCAT(COALESCE(e.firstName, ''), ' ', COALESCE(e.lastName, '')) END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                )
             )
    """)
    Page<Tuple> findTransfers(@Param("filter") TransferRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            prd.productId AS productId,
            prd.productCode AS productCode,
            prd.barCode AS barCode,
            prd.productNameEn AS productNameEn,
            prd.productNameKh AS productNameKh,
            b.name AS brand,
            c.name AS category,
            u.unitCode AS unitCode,
            u.unitNameEn AS unitNameEn,
            u.unitNameKh AS unitNameKh,
            COALESCE(beginning.quantity, 0) AS beginning,
            COALESCE(purchase.quantity, 0) AS purchase,
            COALESCE(transferIn.quantity, 0) AS transferIn,
            COALESCE(transferOut.quantity, 0) AS transferOut,
            COALESCE(adjustmentAdd.quantity, 0) AS adjustmentAdd,
            COALESCE(adjustmentSub.quantity, 0) AS adjustmentSub,
            COALESCE(sold.quantity, 0) AS sold,
            (
                COALESCE(beginning.quantity, 0) +
                COALESCE(purchase.quantity, 0) +
                COALESCE(transferIn.quantity, 0) -
                COALESCE(transferOut.quantity, 0) +
                COALESCE(adjustmentAdd.quantity, 0) -
                COALESCE(adjustmentSub.quantity, 0) -
                COALESCE(sold.quantity, 0)
            ) AS balance
        FROM ProductEntity prd
        LEFT JOIN BrandEntity b ON b.brandId = prd.brandId
        LEFT JOIN CategoryEntity c ON c.categoryId = prd.categoryId
        LEFT JOIN (
            SELECT
                _u.unitId AS unitId, _u.unitCode AS unitCode, _u.unitNameEn AS unitNameEn, _u.unitNameKh AS unitNameKh, _pu.id.productId AS productId
            FROM UnitEntity _u
            INNER JOIN ProductUnitEntity _pu ON _pu.id.unitId = _u.unitId AND _u.punitId IS NULL
        ) u ON u.productId = prd.productId
        LEFT JOIN (
            SELECT
                _prd.productId AS productId,
                (
                    COALESCE(purchase.quantity, 0) +
                    COALESCE(transferIn.quantity, 0) -
                    COALESCE(transferOut.quantity, 0) +
                    COALESCE(adjustmentAdd.quantity, 0) -
                    COALESCE(adjustmentSub.quantity, 0) -
                    COALESCE(sold.quantity, 0)
                ) AS quantity
            FROM ProductEntity _prd
            LEFT JOIN (
                SELECT
                    pi.productId AS productId,
                    COALESCE(SUM(pi.quantity), 0) AS quantity
                FROM PurchaseEntity p
                INNER JOIN PurchaseItemEntity pi ON pi.purchaseId = p.id
                WHERE
                    p.status != 'pending' AND
                    (:#{#filter.bIds} IS NULL OR p.billerId IN :#{#filter.bIds}) AND
                    (:#{#filter.whIds} IS NULL OR p.warehouseId IN :#{#filter.whIds}) AND
                    (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR p.billerId = :#{#filter.billerId}) AND
                    (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR p.warehouseId = :#{#filter.warehouseId}) AND
                    (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR pi.productId = :#{#filter.productId}) AND
                    (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR p.date < :#{#filter.start})
                GROUP BY pi.productId
            ) purchase ON purchase.productId = _prd.productId
            LEFT JOIN (
                SELECT
                    tri.productId AS productId,
                    COALESCE(SUM(tri.quantity), 0) AS quantity
                FROM TransferEntity tr
                INNER JOIN TransferItemEntity tri ON tri.transferId = tr.id
                WHERE
                    tr.status != 'pending' AND
                    (:#{#filter.bIds} IS NULL OR (tr.fromBiller IN :#{#filter.bIds} OR tr.toBiller IN :#{#filter.bIds})) AND
                    (:#{#filter.whIds} IS NULL OR (tr.fromWarehouse IN :#{#filter.whIds} OR tr.toWarehouse IN :#{#filter.whIds})) AND
                    (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR tr.toBiller = :#{#filter.billerId}) AND
                    (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR tr.toWarehouse = :#{#filter.warehouseId}) AND
                    (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR tri.productId = :#{#filter.productId}) AND
                    (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR tr.date < :#{#filter.start})
                GROUP BY tri.productId
            ) transferIn ON transferIn.productId = _prd.productId
            LEFT JOIN (
                SELECT
                    tri.productId AS productId,
                    COALESCE(SUM(tri.quantity), 0) AS quantity
                FROM TransferEntity tr
                INNER JOIN TransferItemEntity tri ON tri.transferId = tr.id
                WHERE
                    tr.status != 'pending' AND
                    (:#{#filter.bIds} IS NULL OR (tr.fromBiller IN :#{#filter.bIds} OR tr.toBiller IN :#{#filter.bIds})) AND
                    (:#{#filter.whIds} IS NULL OR (tr.fromWarehouse IN :#{#filter.whIds} OR tr.toWarehouse IN :#{#filter.whIds})) AND
                    (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR tr.fromBiller = :#{#filter.billerId}) AND
                    (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR tr.fromWarehouse = :#{#filter.warehouseId}) AND
                    (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR tri.productId = :#{#filter.productId}) AND
                    (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR tr.date < :#{#filter.start})
                GROUP BY tri.productId
            ) transferOut ON transferOut.productId = _prd.productId
            LEFT JOIN (
                SELECT
                    aji.productId AS productId,
                    COALESCE(SUM(aji.quantity), 0) AS quantity
                FROM AdjustmentEntity aj
                INNER JOIN AdjustmentItemEntity aji ON aji.adjustment.id = aj.id
                WHERE
                    aji.type = 'addition' AND
                    (:#{#filter.bIds} IS NULL OR aj.billerId IN :#{#filter.bIds}) AND
                    (:#{#filter.whIds} IS NULL OR aj.warehouseId IN :#{#filter.whIds}) AND
                    (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR aj.billerId = :#{#filter.billerId}) AND
                    (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR aj.warehouseId = :#{#filter.warehouseId}) AND
                    (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR aji.productId = :#{#filter.productId}) AND
                    (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR aj.date < :#{#filter.start})
                GROUP BY aji.productId
            ) adjustmentAdd ON adjustmentAdd.productId = _prd.productId
            LEFT JOIN (
                SELECT
                    aji.productId AS productId,
                    COALESCE(SUM(aji.quantity), 0) AS quantity
                FROM AdjustmentEntity aj
                INNER JOIN AdjustmentItemEntity aji ON aji.adjustment.id = aj.id
                WHERE
                    aji.type = 'subtraction' AND
                    (:#{#filter.bIds} IS NULL OR aj.billerId IN :#{#filter.bIds}) AND
                    (:#{#filter.whIds} IS NULL OR aj.warehouseId IN :#{#filter.whIds}) AND
                    (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR aj.billerId = :#{#filter.billerId}) AND
                    (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR aj.warehouseId = :#{#filter.warehouseId}) AND
                    (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR aji.productId = :#{#filter.productId}) AND
                    (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR aj.date < :#{#filter.start})
                GROUP BY aji.productId
            ) adjustmentSub ON adjustmentSub.productId = _prd.productId
            LEFT JOIN (
                SELECT
                    si.productId AS productId,
                    COALESCE(SUM(si.quantity), 0) AS quantity
                FROM SaleEntity s
                INNER JOIN SaleItemEntity si ON si.saleId = s.id
                WHERE
                    s.status != 'pending' AND
                    (:#{#filter.bIds} IS NULL OR s.billerId IN :#{#filter.bIds}) AND
                    (:#{#filter.whIds} IS NULL OR s.warehouseId IN :#{#filter.whIds}) AND
                    (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR s.billerId = :#{#filter.billerId}) AND
                    (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR s.warehouseId = :#{#filter.warehouseId}) AND
                    (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR si.productId = :#{#filter.productId}) AND
                    (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR s.date < :#{#filter.start})
                GROUP BY si.productId
            ) sold ON sold.productId = _prd.productId
        ) beginning ON beginning.productId = prd.productId
        LEFT JOIN (
            SELECT
                pi.productId AS productId,
                COALESCE(SUM(pi.quantity), 0) AS quantity
            FROM PurchaseEntity p
            INNER JOIN PurchaseItemEntity pi ON pi.purchaseId = p.id
            WHERE
                p.status != 'pending' AND
                (:#{#filter.bIds} IS NULL OR p.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.whIds} IS NULL OR p.warehouseId IN :#{#filter.whIds}) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR p.billerId = :#{#filter.billerId}) AND
                (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR p.warehouseId = :#{#filter.warehouseId}) AND
                (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR pi.productId = :#{#filter.productId}) AND
                (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (p.date BETWEEN :#{#filter.start} AND :#{#filter.end}))
            GROUP BY pi.productId
        ) purchase ON purchase.productId = prd.productId
        LEFT JOIN (
            SELECT
                tri.productId AS productId,
                COALESCE(SUM(tri.quantity), 0) AS quantity
            FROM TransferEntity tr
            INNER JOIN TransferItemEntity tri ON tri.transferId = tr.id
            WHERE
                tr.status != 'pending' AND
                (:#{#filter.bIds} IS NULL OR (tr.fromBiller IN :#{#filter.bIds} OR tr.toBiller IN :#{#filter.bIds})) AND
                (:#{#filter.whIds} IS NULL OR (tr.fromWarehouse IN :#{#filter.whIds} OR tr.toWarehouse IN :#{#filter.whIds})) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR tr.toBiller = :#{#filter.billerId}) AND
                (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR tr.toWarehouse = :#{#filter.warehouseId}) AND
                (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR tri.productId = :#{#filter.productId}) AND
                (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (tr.date BETWEEN :#{#filter.start} AND :#{#filter.end}))
            GROUP BY tri.productId
        ) transferIn ON transferIn.productId = prd.productId
        LEFT JOIN (
            SELECT
                tri.productId AS productId,
                COALESCE(SUM(tri.quantity), 0) AS quantity
            FROM TransferEntity tr
            INNER JOIN TransferItemEntity tri ON tri.transferId = tr.id
            WHERE
                tr.status != 'pending' AND
                (:#{#filter.bIds} IS NULL OR (tr.fromBiller IN :#{#filter.bIds} OR tr.toBiller IN :#{#filter.bIds})) AND
                (:#{#filter.whIds} IS NULL OR (tr.fromWarehouse IN :#{#filter.whIds} OR tr.toWarehouse IN :#{#filter.whIds})) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR tr.fromBiller = :#{#filter.billerId}) AND
                (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR tr.fromWarehouse = :#{#filter.warehouseId}) AND
                (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR tri.productId = :#{#filter.productId}) AND
                (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (tr.date BETWEEN :#{#filter.start} AND :#{#filter.end}))
            GROUP BY tri.productId
        ) transferOut ON transferOut.productId = prd.productId
        LEFT JOIN (
            SELECT
                aji.productId AS productId,
                COALESCE(SUM(aji.quantity), 0) AS quantity
            FROM AdjustmentEntity aj
            INNER JOIN AdjustmentItemEntity aji ON aji.adjustment.id = aj.id
            WHERE
                aji.type = 'addition' AND
                (:#{#filter.bIds} IS NULL OR aj.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.whIds} IS NULL OR aj.warehouseId IN :#{#filter.whIds}) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR aj.billerId = :#{#filter.billerId}) AND
                (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR aj.warehouseId = :#{#filter.warehouseId}) AND
                (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR aji.productId = :#{#filter.productId}) AND
                (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (aj.date BETWEEN :#{#filter.start} AND :#{#filter.end}))
            GROUP BY aji.productId
        ) adjustmentAdd ON adjustmentAdd.productId = prd.productId
        LEFT JOIN (
            SELECT
                aji.productId AS productId,
                COALESCE(SUM(aji.quantity), 0) AS quantity
            FROM AdjustmentEntity aj
            INNER JOIN AdjustmentItemEntity aji ON aji.adjustment.id = aj.id
            WHERE
                aji.type = 'subtraction' AND
                (:#{#filter.bIds} IS NULL OR aj.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.whIds} IS NULL OR aj.warehouseId IN :#{#filter.whIds}) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR aj.billerId = :#{#filter.billerId}) AND
                (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR aj.warehouseId = :#{#filter.warehouseId}) AND
                (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR aji.productId = :#{#filter.productId}) AND
                (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (aj.date BETWEEN :#{#filter.start} AND :#{#filter.end}))
            GROUP BY aji.productId
        ) adjustmentSub ON adjustmentSub.productId = prd.productId
        LEFT JOIN (
            SELECT
                si.productId AS productId,
                COALESCE(SUM(si.quantity), 0) AS quantity
            FROM SaleEntity s
            INNER JOIN SaleItemEntity si ON si.saleId = s.id
            WHERE
                s.status != 'pending' AND
                (:#{#filter.bIds} IS NULL OR s.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.whIds} IS NULL OR s.warehouseId IN :#{#filter.whIds}) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR s.billerId = :#{#filter.billerId}) AND
                (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR s.warehouseId = :#{#filter.warehouseId}) AND
                (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR si.productId = :#{#filter.productId}) AND
                (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (s.date BETWEEN :#{#filter.start} AND :#{#filter.end}))
            GROUP BY si.productId
        ) sold ON sold.productId = prd.productId
        WHERE
            prd.status = 1 AND prd.type = 'standard' AND
            (:#{#filter.productId} IS NULL OR :#{#filter.productId} = '' OR prd.productId = :#{#filter.productId}) AND
            (:#{#filter.productCode} IS NULL OR :#{#filter.productCode} = '' OR LOWER(prd.productCode) LIKE LOWER(CONCAT('%', :#{#filter.productCode}, '%'))) AND
            (:#{#filter.barCode} IS NULL OR :#{#filter.barCode} = '' OR LOWER(prd.barCode) LIKE LOWER(CONCAT('%', :#{#filter.barCode}, '%'))) AND
            (:#{#filter.productNameEn} IS NULL OR :#{#filter.productNameEn} = '' OR LOWER(prd.productNameEn) LIKE LOWER(CONCAT('%', :#{#filter.productNameEn}, '%'))) AND
            (:#{#filter.productNameKh} IS NULL OR :#{#filter.productNameKh} = '' OR LOWER(prd.productNameKh) LIKE LOWER(CONCAT('%', :#{#filter.productNameKh}, '%'))) AND
            (:#{#filter.brandId} IS NULL OR :#{#filter.brandId} = '' OR prd.brandId = :#{#filter.brandId}) AND
            (:#{#filter.categoryId} IS NULL OR :#{#filter.categoryId} = '' OR prd.categoryId = :#{#filter.categoryId}) AND
            (:#{#filter.type} IS NULL OR :#{#filter.type} = '' OR prd.type = :#{#filter.type}) AND
            (
                (:#{#filter.all} IS NULL OR :#{#filter.all} = 1) OR
                (
                    (:#{#filter.all} IS NOT NULL AND :#{#filter.all} != 1) AND
                    (
                        (purchase.quantity IS NOT NULL AND purchase.quantity != 0) OR
                        (transferIn.quantity IS NOT NULL AND transferIn.quantity != 0) OR
                        (transferOut.quantity IS NOT NULL AND transferOut.quantity != 0) OR
                        (adjustmentAdd.quantity IS NOT NULL AND adjustmentAdd.quantity != 0) OR
                        (adjustmentSub.quantity IS NOT NULL AND adjustmentSub.quantity != 0) OR
                        (sold.quantity IS NOT NULL AND sold.quantity != 0)
                    )
                )
            ) AND
            (
                (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') OR
                (
                    LOWER(prd.productCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(prd.barCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(prd.productNameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(prd.productNameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(prd.type) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(c.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(b.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(u.unitCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(u.unitNameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(u.unitNameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                )
            )
    """)
    Page<Tuple> findProducts(@Param("filter") ProductRequest filter, Pageable pageable);
}
