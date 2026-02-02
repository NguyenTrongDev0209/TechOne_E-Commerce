package com.techone.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "accounts")
public class Account {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	
	@Column(columnDefinition = "varchar(255)")
	public String provider;
	
	@Column(columnDefinition = "varchar(12)", unique = true)
	public String phone;
	
	@Column(columnDefinition = "varchar(12)", unique = true)
	@NotBlank(message = "Email không được để trống")
	@Email(message = "Email nhập chưa đúng định dạng")
	public String email;
	
	@Column(columnDefinition = "varchar(255)")
	@NotBlank(message = "Email không được để trống")
	public String password;
	
	@Column(columnDefinition = "varchar(255)")
	public String avatar;
	
	@Past(message = "Ngày sinh phải là một ngày trong quá khứ")
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	public LocalDate birthday;
	
	public Integer status;
	
	public Boolean role = false;
	//False = User; True = Admin
	
	@PastOrPresent(message = "Ngày tạo không thể ở tương lai")
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	public LocalDate createAt = LocalDate.now();
	//Lấy ngày tạo tài khoản là ngày hôm đó luôn
}
