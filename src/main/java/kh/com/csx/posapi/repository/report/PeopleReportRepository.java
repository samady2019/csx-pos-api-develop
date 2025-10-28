package kh.com.csx.posapi.repository.report;

import jakarta.persistence.Tuple;
import kh.com.csx.posapi.dto.report.peopleReport.*;
import kh.com.csx.posapi.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PeopleReportRepository extends JpaRepository<UserEntity, Long> {
    @Query(value = """
        SELECT
            spp.id AS id,
            spp.companyEn AS companyEn,
            spp.companyKh AS companyKh,
            spp.nameEn AS nameEn,
            spp.nameKh AS nameKh,
            spp.gender AS gender,
            spp.contactPerson AS contactPerson,
            spp.phone AS phone,
            spp.email AS email,
            COALESCE(p.count, 0) AS totalPurchases,
            COALESCE(p.grandTotal, 0) AS totalAmount,
            COALESCE(p.paid, 0) AS totalPaid,
            COALESCE(p.balance, 0) AS totalBalance
        FROM SupplierEntity spp
        LEFT JOIN (
            SELECT
                _p.supplierId AS supplierId,
                COALESCE(SUM(_p.grandTotal), 0) AS grandTotal,
                COALESCE(SUM(_p.paid), 0) AS paid,
                COALESCE(SUM(_p.grandTotal), 0) - COALESCE(SUM(_p.paid), 0) AS balance,
                COUNT(_p.id) AS count
            FROM PurchaseEntity _p
            WHERE
                _p.status != 'pending' AND
                (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR _p.createdBy = :#{#filter.user}) AND
                (:#{#filter.bIds} IS NULL OR _p.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.whIds} IS NULL OR _p.warehouseId IN :#{#filter.whIds}) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR _p.billerId = :#{#filter.billerId}) AND
                (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR _p.warehouseId = :#{#filter.warehouseId}) AND
                (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR _p.createdBy = :#{#filter.createdBy}) AND
                (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR _p.updatedBy = :#{#filter.updatedBy}) AND
                (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (_p.date BETWEEN :#{#filter.start} AND :#{#filter.end}))
            GROUP BY _p.supplierId
        ) p ON p.supplierId = spp.id
        WHERE
            (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR spp.id = :#{#filter.id}) AND
            (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR
                (
                    LOWER(spp.companyEn) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')) OR
                    LOWER(spp.companyKh) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')) OR
                    LOWER(spp.nameEn) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')) OR
                    LOWER(spp.nameKh) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%'))
                )
            ) AND
            (:#{#filter.gender} IS NULL OR :#{#filter.gender} = '' OR spp.gender = :#{#filter.gender}) AND
            (:#{#filter.contactPerson} IS NULL OR :#{#filter.contactPerson} = '' OR REPLACE(spp.contactPerson, ' ', '') LIKE CONCAT('%', REPLACE(:#{#filter.contactPerson}, ' ', ''), '%')) AND
            (:#{#filter.phone} IS NULL OR :#{#filter.phone} = '' OR REPLACE(spp.phone, ' ', '') LIKE CONCAT('%', REPLACE(:#{#filter.phone}, ' ', ''), '%')) AND
            (:#{#filter.email} IS NULL OR :#{#filter.email} = '' OR spp.email LIKE CONCAT('%', :#{#filter.email}, '%')) AND
            (
                (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') OR
                (
                    LOWER(spp.companyEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(spp.companyKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(spp.nameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(spp.nameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(spp.vatNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(spp.gender) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(REPLACE(spp.contactPerson, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(TRIM(:#{#filter.term}), ' ', ''), '%')) OR
                    LOWER(REPLACE(spp.phone, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(TRIM(:#{#filter.term}), ' ', ''), '%')) OR
                    LOWER(spp.email) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(spp.addressKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(spp.addressEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                )
            )
    """)
    Page<Tuple> findSuppliers(@Param("filter") PeopleRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            cus.id AS id,
            cus.companyEn AS companyEn,
            cus.companyKh AS companyKh,
            cus.nameEn AS nameEn,
            cus.nameKh AS nameKh,
            cus.gender AS gender,
            cus.contactPerson AS contactPerson,
            cus.phone AS phone,
            cus.email AS email,
            COALESCE(s.count, 0) AS totalSales,
            COALESCE(s.grandTotal, 0) AS totalAmount,
            COALESCE(s.paid, 0) AS totalPaid,
            COALESCE(s.balance, 0) AS totalBalance
        FROM CustomerEntity cus
        LEFT JOIN (
            SELECT
                _s.customerId AS customerId,
                COALESCE(SUM(_s.grandTotal), 0) AS grandTotal,
                COALESCE(SUM(_s.paid), 0) AS paid,
                COALESCE(SUM(_s.grandTotal), 0) - COALESCE(SUM(_s.paid), 0) AS balance,
                COUNT(_s.id) AS count
            FROM SaleEntity _s
            WHERE
                _s.status != 'pending' AND
                (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR _s.createdBy = :#{#filter.user}) AND
                (:#{#filter.bIds} IS NULL OR _s.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.whIds} IS NULL OR _s.warehouseId IN :#{#filter.whIds}) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR _s.billerId = :#{#filter.billerId}) AND
                (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR _s.warehouseId = :#{#filter.warehouseId}) AND
                (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR _s.createdBy = :#{#filter.createdBy}) AND
                (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR _s.updatedBy = :#{#filter.updatedBy}) AND
                (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (_s.date BETWEEN :#{#filter.start} AND :#{#filter.end}))
            GROUP BY _s.customerId
        ) s ON s.customerId = cus.id
        WHERE
            (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR cus.id = :#{#filter.id}) AND
            (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR
                (
                    LOWER(cus.companyEn) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')) OR
                    LOWER(cus.companyKh) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')) OR
                    LOWER(cus.nameEn) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')) OR
                    LOWER(cus.nameKh) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%'))
                )
            ) AND
            (:#{#filter.gender} IS NULL OR :#{#filter.gender} = '' OR cus.gender = :#{#filter.gender}) AND
            (:#{#filter.contactPerson} IS NULL OR :#{#filter.contactPerson} = '' OR REPLACE(cus.contactPerson, ' ', '') LIKE CONCAT('%', REPLACE(:#{#filter.contactPerson}, ' ', ''), '%')) AND
            (:#{#filter.phone} IS NULL OR :#{#filter.phone} = '' OR REPLACE(cus.phone, ' ', '') LIKE CONCAT('%', REPLACE(:#{#filter.phone}, ' ', ''), '%')) AND
            (:#{#filter.email} IS NULL OR :#{#filter.email} = '' OR cus.email LIKE CONCAT('%', :#{#filter.email}, '%')) AND
            (
                (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') OR
                (
                    LOWER(cus.nameEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(cus.nameKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(cus.companyEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(cus.companyKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(cus.vatNo) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(cus.gender) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(REPLACE(cus.contactPerson, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(TRIM(:#{#filter.term}), ' ', ''), '%')) OR
                    LOWER(REPLACE(cus.phone, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(TRIM(:#{#filter.term}), ' ', ''), '%')) OR
                    LOWER(cus.email) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(cus.addressKh) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(cus.addressEn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                )
            )
    """)
    Page<Tuple> findCustomers(@Param("filter") PeopleRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            u.userId AS userId,
            u.employeeId AS employeeId,
            e.firstName AS firstName,
            e.lastName AS lastName,
            e.gender AS gender,
            e.dob AS dob,
            e.phone AS phone,
            e.email AS email,
            u.username AS username,
            u.userType AS userType,
            u.status AS status,
            COALESCE(s.commissions, 0) AS totalCommissions,
            COALESCE(s.count, 0) AS totalSales,
            COALESCE(s.grandTotal, 0) AS totalAmount,
            COALESCE(s.paid, 0) AS totalPaid,
            COALESCE(s.balance, 0) AS totalBalance
        FROM UserEntity u
        LEFT JOIN EmployeeEntity e ON e.id = u.employeeId
        LEFT JOIN (
            SELECT
                _s.salesmanBy AS salesmanBy,
                COALESCE(SUM(_s.grandTotal), 0) AS grandTotal,
                COALESCE(SUM(_s.paid), 0) AS paid,
                COALESCE(SUM(_s.grandTotal), 0) - COALESCE(SUM(_s.paid), 0) AS balance,
                0 AS commissions,
                COUNT(_s.id) AS count
            FROM SaleEntity _s
            WHERE
                _s.status != 'pending' AND _s.salesmanBy IS NOT NULL AND
                (:#{#filter.user} IS NULL OR :#{#filter.user} = '' OR _s.createdBy = :#{#filter.user}) AND
                (:#{#filter.bIds} IS NULL OR _s.billerId IN :#{#filter.bIds}) AND
                (:#{#filter.whIds} IS NULL OR _s.warehouseId IN :#{#filter.whIds}) AND
                (:#{#filter.billerId} IS NULL OR :#{#filter.billerId} = '' OR _s.billerId = :#{#filter.billerId}) AND
                (:#{#filter.warehouseId} IS NULL OR :#{#filter.warehouseId} = '' OR _s.warehouseId = :#{#filter.warehouseId}) AND
                (:#{#filter.createdBy} IS NULL OR :#{#filter.createdBy} = '' OR _s.createdBy = :#{#filter.createdBy}) AND
                (:#{#filter.updatedBy} IS NULL OR :#{#filter.updatedBy} = '' OR _s.updatedBy = :#{#filter.updatedBy}) AND
                (:#{#filter.startDate} IS NULL OR :#{#filter.startDate} = '' OR (_s.date BETWEEN :#{#filter.start} AND :#{#filter.end}))
            GROUP BY _s.salesmanBy
        ) s ON s.salesmanBy = u.userId
        WHERE
            u.userType = '2' AND
            (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR u.userId = :#{#filter.id}) AND
            (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR
                (
                    LOWER(u.username) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')) OR
                    LOWER(e.firstName) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')) OR
                    LOWER(e.lastName) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')) OR
                    LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')) OR
                    LOWER(CONCAT(e.lastName, ' ', e.firstName)) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%'))
                )
            ) AND
            (:#{#filter.gender} IS NULL OR :#{#filter.gender} = '' OR e.gender = :#{#filter.gender}) AND
            (:#{#filter.phone} IS NULL OR :#{#filter.phone} = '' OR REPLACE(e.phone, ' ', '') LIKE CONCAT('%', REPLACE(:#{#filter.phone}, ' ', ''), '%')) AND
            (:#{#filter.email} IS NULL OR :#{#filter.email} = '' OR e.email LIKE CONCAT('%', :#{#filter.email}, '%')) AND
            (:#{#filter.status} IS NULL OR :#{#filter.status} = '' OR u.status = :#{#filter.status}) AND
            (
                (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') OR
                (
                    LOWER(u.username) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(e.gender) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(REPLACE(e.phone, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(TRIM(:#{#filter.term}), ' ', ''), '%')) OR
                    LOWER(e.email) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CASE WHEN u.status = '1' THEN 'active' ELSE 'inactive' END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CONCAT(COALESCE(e.firstName, ''), ' ', COALESCE(e.lastName, ''))) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                )
            )
    """)
    Page<Tuple> findSalesman(@Param("filter") UserRequest filter, Pageable pageable);

    @Query(value = """
        SELECT
            u.userId AS userId,
            u.employeeId AS employeeId,
            e.firstName AS firstName,
            e.lastName AS lastName,
            e.gender AS gender,
            e.dob AS dob,
            e.phone AS phone,
            e.email AS email,
            u.username AS username,
            u.userType AS userType,
            u.status AS status
        FROM UserEntity u
        LEFT JOIN EmployeeEntity e ON e.id = u.employeeId
        WHERE
            (:#{#filter.id} IS NULL OR :#{#filter.id} = '' OR u.userId = :#{#filter.id}) AND
            (:#{#filter.name} IS NULL OR :#{#filter.name} = '' OR
                (
                    LOWER(u.username) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')) OR
                    LOWER(e.firstName) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')) OR
                    LOWER(e.lastName) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')) OR
                    LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')) OR
                    LOWER(CONCAT(e.lastName, ' ', e.firstName)) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%'))
                )
            ) AND
            (:#{#filter.gender} IS NULL OR :#{#filter.gender} = '' OR e.gender = :#{#filter.gender}) AND
            (:#{#filter.phone} IS NULL OR :#{#filter.phone} = '' OR REPLACE(e.phone, ' ', '') LIKE CONCAT('%', REPLACE(:#{#filter.phone}, ' ', ''), '%')) AND
            (:#{#filter.email} IS NULL OR :#{#filter.email} = '' OR e.email LIKE CONCAT('%', :#{#filter.email}, '%')) AND
            (:#{#filter.status} IS NULL OR :#{#filter.status} = '' OR u.status = :#{#filter.status}) AND
            (
                (:#{#filter.term} IS NULL OR TRIM(:#{#filter.term}) = '') OR
                (
                    LOWER(u.username) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(e.gender) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(REPLACE(e.phone, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(TRIM(:#{#filter.term}), ' ', ''), '%')) OR
                    LOWER(e.email) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CASE WHEN u.status = '1' THEN 'active' ELSE 'inactive' END) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%')) OR
                    LOWER(CONCAT(COALESCE(e.firstName, ''), ' ', COALESCE(e.lastName, ''))) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.term}), '%'))
                )
            )
    """)
    Page<Tuple> findUsers(@Param("filter") UserRequest filter, Pageable pageable);
}
