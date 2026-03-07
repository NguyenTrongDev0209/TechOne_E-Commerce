package com.techone.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
