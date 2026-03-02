package com.techone.model;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "shipments")
public class Shipment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;

	@Column(columnDefinition = "varchar(100)")
	public String trackingNumber;

	@Column(columnDefinition = "nvarchar(255)")
	public String carrier;

	public Integer status;

	@PastOrPresent(message = "Ngày tạo Oder không thể ở tương lai")
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	public LocalDateTime createAt = LocalDateTime.now();

	@FutureOrPresent(message = "Ngày giao dự kiến không thể ở quá khứ")
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	public LocalDateTime estimatedDeliveryAt;

	@ManyToOne
	@JoinColumn(name = "address_id")
	Address address;

	@ManyToOne
	@JoinColumn(name = "order_id")
	Order order;
}
