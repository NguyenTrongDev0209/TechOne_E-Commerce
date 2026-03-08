package com.techone.domain.order.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techone.domain.user.entity.Account;
import com.techone.domain.order.entity.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByAccount(Account account);

    Optional<Cart> findByAccountId(Integer accountId);
}
