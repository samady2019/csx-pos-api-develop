package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.entity.TaxItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaxItemRepository extends JpaRepository<TaxItemEntity, Long> {
    List<TaxItemEntity> findByTransactionAndTransactionId(String transaction, Long transactionId);

    @Modifying
    @Query("DELETE FROM TaxItemEntity ti WHERE ti.transaction = :transaction AND ti.transactionId = :transactionId")
    void deleteByTransactionAndTransactionId(String transaction, Long transactionId);
}
