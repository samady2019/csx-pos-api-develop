package kh.com.csx.posapi.repository;

import kh.com.csx.posapi.entity.MembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipRepository extends JpaRepository<MembershipEntity, Long> {
    List<MembershipEntity> findByCustomerId(Long customerId);


}
