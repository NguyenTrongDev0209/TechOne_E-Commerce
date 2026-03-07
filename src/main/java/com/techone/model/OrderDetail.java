package com.techone.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "order_details")
public class OrderDetail {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	
	@PositiveOrZero(message = "Giá không thể âm")
	public Double unitPrice;
	
	@PositiveOrZero(message = "Số lượng không thể âm")
	public Integer quantity;
	
	@ManyToOne
	@JoinColumn(name = "order_id")
	Order order;
	
	@ManyToOne
	@JoinColumn(name = "variant_id")
	Variant variant;
}
