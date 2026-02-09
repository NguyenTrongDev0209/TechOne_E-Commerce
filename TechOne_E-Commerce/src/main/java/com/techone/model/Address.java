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
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "addresss")
public class Address {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	
	@Column(columnDefinition = "nvarchar(255)")
	@NotBlank(message = "Full address không thể để trống")
	public String fullAddress;
	
	@FutureOrPresent(message = "Ngày tạo Oder không thể ở quá khứ")
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	public LocalDateTime createAt = LocalDateTime.now();
	
	public Boolean status;
	
	@Column(columnDefinition = "nvarchar(100)")
	@NotBlank
	public String name;
	
	@Column(columnDefinition = "varchar(12)", unique = true)
	public String phone;
	
	@ManyToOne
	@JoinColumn(name = "ward_id")
	Ward ward;
	
	@ManyToOne
	@JoinColumn(name = "account_id")
	Account account;
}
