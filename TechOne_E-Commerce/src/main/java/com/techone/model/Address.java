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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
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
	@NotBlank(message = "Địa chỉ cụ thể không thể để trống")
	public String fullAddress;

	@PastOrPresent(message = "Ngày tạo địa chỉ không thể ở tương lai")
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	public LocalDateTime createAt = LocalDateTime.now();

	public Boolean status;

	@Column(columnDefinition = "nvarchar(100)")
	@NotBlank(message = "Tên không được để trống")
	public String name;

	@Column(columnDefinition = "varchar(12)")
	@NotBlank(message = "Số điện thoại không được để trống")
	@Pattern(regexp = "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$", message = "Số điện thoại không đúng định dạng")
	public String phone;

	@ManyToOne
	@JoinColumn(name = "ward_id")
	Ward ward;

	@ManyToOne
	@JoinColumn(name = "account_id")
	Account account;

	public String toJson() {
		return String.format(
				"{\"id\":%d,\"name\":\"%s\",\"phone\":\"%s\",\"fullAddress\":\"%s\",\"status\":%b," +
						"\"ward\":{\"id\":\"%s\",\"name\":\"%s\"," +
						"\"district\":{\"id\":%d,\"name\":\"%s\"," +
						"\"province\":{\"id\":%d,\"name\":\"%s\"}}}}",
				id, name.replace("\"", "\\\""), phone, fullAddress.replace("\"", "\\\""), status,
				ward.id, ward.name.replace("\"", "\\\""),
				ward.district.id, ward.district.name.replace("\"", "\\\""),
				ward.district.province.id, ward.district.province.name.replace("\"", "\\\""));
	}
}
