package com.techone.domain.user.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techone.domain.user.entity.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    List<Address> findByAccountIdOrderByStatusDesc(Integer accountId);

    Optional<Address> findByAccountIdAndStatusTrue(Integer accountId);
}
