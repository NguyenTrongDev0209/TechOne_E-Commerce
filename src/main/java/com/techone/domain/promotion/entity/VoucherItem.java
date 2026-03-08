package com.techone.domain.promotion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.techone.domain.user.entity.Account;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "voucher_items")
public class VoucherItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @ManyToOne
    @JoinColumn(name = "voucher_percent_id")
    VoucherPercent voucherPercent;

    // 0: Chưa sử dụng, 1: Đã sử dụng
    public Integer status;

    @ManyToOne
    @JoinColumn(name = "account_id")
    Account account;
}
