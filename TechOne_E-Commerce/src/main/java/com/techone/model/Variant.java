package com.techone.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "variants")
public class Variant {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	
	@PositiveOrZero(message = "Giá không thể âm")
	public Double price;
	
	@PositiveOrZero(message = "Số lượng tồn không thể âm")
	public Integer stock;

	public Integer status;
	
	@Column(columnDefinition = "varchar(255)")
	@NotBlank(message = "Mã SKU không thể bỏ trống")
	public String sku;
	
	@ManyToOne
	@JoinColumn(name = "product_id")
	Product product;
	
	@ManyToOne
	@JoinColumn(name = "account_id")
	Account account;
	
	@OneToMany(mappedBy = "variant")
	public List<Favourite> favourite;
	
	@OneToMany(mappedBy = "variant")
	public List<CartItem> cartItem;
	
	@OneToMany(mappedBy = "variant")
	public List<OrderDetail> orderDetail;
}
