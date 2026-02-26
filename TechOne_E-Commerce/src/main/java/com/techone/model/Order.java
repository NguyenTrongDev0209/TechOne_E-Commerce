package com.techone.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "orders")
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;

	@PastOrPresent(message = "Ngày tạo Oder không thể ở tương lai")
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	public LocalDateTime createAt = LocalDateTime.now();

	public Integer status;

	@Column(columnDefinition = "nvarchar(max)")
	public String note;

	@OneToMany(mappedBy = "order")
	public List<OrderDetail> orderDetail;

	@OneToMany(mappedBy = "order")
	public List<Shipment> shipment;

	@OneToMany(mappedBy = "order")
	public List<Transaction> transaction;

	@ManyToOne
	@JoinColumn(name = "account_id")
	public Account account;

	public Double shippingFee = 0.0;
	public Double totalAmount = 0.0;
	public Double voucherDiscount = 0.0;

	public Double getTotalPrice() {
		if (totalAmount != null && totalAmount > 0) {
			return totalAmount;
		}
		if (orderDetail == null)
			return 0.0;
		double subtotal = orderDetail.stream()
				.mapToDouble(d -> d.getUnitPrice() * d.getQuantity())
				.sum();
		return subtotal + (shippingFee != null ? shippingFee : 0.0) - (voucherDiscount != null ? voucherDiscount : 0.0);
	}
}
