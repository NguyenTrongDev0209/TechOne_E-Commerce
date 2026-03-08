package com.techone.domain.promotion.repository;

import com.techone.domain.promotion.entity.VoucherPercent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VoucherPercentRepository extends JpaRepository<VoucherPercent, Integer> {
    List<VoucherPercent> findByCode(String code);
}
