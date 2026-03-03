package com.techone.repository;

import com.techone.model.Account;
import com.techone.model.VoucherItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.techone.model.VoucherPercent;
import java.util.Optional;

@Repository
public interface VoucherItemRepository extends JpaRepository<VoucherItem, Integer> {
    List<VoucherItem> findByAccount(Account account);

    Optional<VoucherItem> findByAccountAndVoucherPercent(Account account, VoucherPercent voucherPercent);

    List<VoucherItem> findByAccountAndStatus(Account account, Integer status);

    long countByAccountIdAndStatus(Integer accountId, Integer status);
}
