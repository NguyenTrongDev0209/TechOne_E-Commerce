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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "forgot_password")
public class ForgotPassword {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	
	@Column(columnDefinition = "varchar(6)", unique = true)
	public String otp;
	
	@FutureOrPresent(message = "Ngày hết hạn phải ở hiện tại hoặc tương lai")
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	public LocalDateTime expiryDate;
	
	@ManyToOne
	@JoinColumn(name = "account_id", unique = true)
	Account account;
}
