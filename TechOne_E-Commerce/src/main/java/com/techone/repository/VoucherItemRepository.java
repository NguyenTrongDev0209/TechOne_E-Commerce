package com.techone.repository;

import com.techone.model.Account;
import com.techone.model.VoucherItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherItemRepository extends JpaRepository<VoucherItem, Integer> {
    List<VoucherItem> findByAccount(Account account);
}
