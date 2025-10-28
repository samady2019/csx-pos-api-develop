package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.entity.OrderRefEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
public interface OrderRefRepository extends JpaRepository<OrderRefEntity, Long> {
    @Query(value = """
            SELECT * FROM order_ref WHERE
            biller_id = :billerId AND
            (
                ((:date IS NULL OR :date = '') AND (date IS NULL)) OR
                ((:date IS NOT NULL AND :date != '') AND (date LIKE CONCAT(:date, '%')))
            )
            ORDER BY id DESC LIMIT 1
        """, nativeQuery = true)
    Optional<OrderRefEntity> findByBillerIdAndDate(Long billerId, String date);
}
