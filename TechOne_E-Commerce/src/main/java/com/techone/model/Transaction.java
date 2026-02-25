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
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "transactions")
public class Transaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;

	@PositiveOrZero(message = "Tổng tiền không thể âm")
	public Double amount;

	@Column(columnDefinition = "varchar(255)")
	public String paymentMethod;

	public Integer status;

	@Column(columnDefinition = "varchar(255)")
	public String transactionType;

	public Integer refId;

	@PastOrPresent(message = "Ngày tạo giao dịch không thể ở tương lai")
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	public LocalDateTime createAt = LocalDateTime.now();

	@Column(columnDefinition = "varchar(255)")
	public String log;

	@ManyToOne
	@JoinColumn(name = "order_id")
	Order order;
}
