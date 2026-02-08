package com.techone.model;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "cart_items")
public class CartItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	
	@PositiveOrZero(message = "Số lượng không thể âm")
	public Integer quantity;
	
	public Integer status;
	
	@FutureOrPresent(message = "Ngày tạo không thể ở quá khứ")
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	public LocalDateTime createAt = LocalDateTime.now();
	
	@ManyToOne
	@JoinColumn(name = "variant_id", unique = true)
	Variant variant;
	
	@ManyToOne
	@JoinColumn(name = "account_id", unique = true)
	Account account;
}
