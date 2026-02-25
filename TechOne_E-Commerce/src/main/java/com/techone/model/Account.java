package com.techone.model;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
@Entity
@Table(name = "accounts")
public class Account {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	
	@Column(columnDefinition = "varchar(255)")
	public String provider;
	
	@Column(columnDefinition = "varchar(12)")
	public String phone;
	
	@Column(columnDefinition = "nvarchar(255)")
	@NotBlank(message = "Họ và tên không được để trống")
	public String fullname;
	
	@Transient // Không tạo cột trong DB
//	@NotBlank(message = "Email hoặc Số điện thoại không được để trống")
	public String contact;
	
	@Column(columnDefinition = "varchar(255)")
//	@NotBlank(message = "Email không được để trống")
//	@Email(message = "Email nhập chưa đúng định dạng")
	public String email;
	
	@Column(columnDefinition = "varchar(255)")
	@NotBlank(message = "Password không được để trống")
	public String password;
	
	@Column(columnDefinition = "varchar(255)")
	public String avatar;
	
	@Past(message = "Ngày sinh phải là một ngày trong quá khứ")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	public LocalDate birthday;

	public Boolean gender;
	
	public Integer status;
	
	public Boolean role = false;
	//False = User; True = Admin
	
	@PastOrPresent(message = "Ngày tạo không thể ở tương lai")
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	public LocalDate createAt;
	//Lấy ngày tạo tài khoản là ngày hôm đó luôn
	
	@OneToMany(mappedBy = "account")
	public List<Post> post;
	
	@OneToMany(mappedBy = "account")
	public List<Comment> comment;
	
	@OneToMany(mappedBy = "account")
	public List<Favourite> favourite;
	
	@OneToMany(mappedBy = "account")
	public List<Review> review;
	
	@OneToMany(mappedBy = "account")
	public List<ForgotPassword> forgotPassword;
	
	@OneToMany(mappedBy = "account")
	public List<VoucherList> voucherList;
	
	@OneToMany(mappedBy = "account")
	public List<Variant> variant;
	
	@OneToMany(mappedBy = "account")
	public List<CartItem> cartItem;
	
	@OneToMany(mappedBy = "account")
	public List<Address> address;
}
