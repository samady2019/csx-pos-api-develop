package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.entity.TransferItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferItemRepository extends JpaRepository<TransferItemEntity, Long> {
    @Modifying
    @Query(value = "DELETE FROM transfer_items WHERE transfer_id = :id", nativeQuery = true)
    void deleteByTransferId(@Param("id") Long id);
}
