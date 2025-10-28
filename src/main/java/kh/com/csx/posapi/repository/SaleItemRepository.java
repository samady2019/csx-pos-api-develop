package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.entity.SaleItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface SaleItemRepository extends JpaRepository<SaleItemEntity, Long> {
    Optional<SaleItemEntity> findById(Long id);

    List<SaleItemEntity> findBySaleId(Long saleId);

    @Modifying
    @Query("DELETE FROM SaleItemEntity si WHERE si.saleId = :saleId")
    void deleteBySaleId(@Param("saleId") Long saleId);
}
