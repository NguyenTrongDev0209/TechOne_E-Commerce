package com.techone.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "voucher_percent")
public class VoucherPercent {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	
	@Column(columnDefinition = "nvarchar(255)")
	@NotBlank(message = "Tên voucher không được để trống")
	public String name;
	
	@Column(columnDefinition = "varchar(100)")
	@NotBlank(message = "Code voucher không được để trống")
	public String code;
	
	@FutureOrPresent(message = "Ngày tạo không thể ở quá khứ")
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	public LocalDateTime createAt = LocalDateTime.now();
	
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	public LocalDateTime activeDay;
	
	@Future(message = "Ngày kết th không thể ở tương lai")
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	public LocalDateTime endAt;
	
	public Boolean voucherType;
	
	@PositiveOrZero(message = "Giá giảm không thể âm")
	public Double minPrice;
	
	public Double maxPrice;
	
	public Integer status;
	
	@PositiveOrZero(message = "Phần trăm giảm không thể âm")
	public Double percentVoucher;
	
	public Integer quantity;
	
	@OneToMany(mappedBy = "voucherPercent")
	public List<VoucherList> voucherList;
}
