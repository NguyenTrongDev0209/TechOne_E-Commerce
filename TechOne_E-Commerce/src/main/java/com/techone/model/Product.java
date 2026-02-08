package com.techone.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

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
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "products")
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	
	@Column(columnDefinition = "nvarchar(255)")
	@NotBlank(message = "Tên sản phẩm không được để trống")
	public String name;
	
	@Column(columnDefinition = "varchar(255)")
	public String slug;
	
	@PastOrPresent(message = "Ngày tạo phải là ngày trong quá khứ hoặc hiện tại")
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	public LocalDateTime createAt = LocalDateTime.now();
	
	public Integer status;
	
	@Column(columnDefinition = "nvarchar(max)")
	@NotBlank(message = "Nội dung mô tả không thể để trống")
	public String content;
	
	@ManyToOne
	@JoinColumn(name = "category_id")
	Category category;
	
	@ManyToOne
	@JoinColumn(name = "brand_id")
	Brand brand;
	
	public String specifications;
	
	@OneToMany(mappedBy = "product")
	public List<Sale> sale;
	
	@OneToMany(mappedBy = "product")
	public List<Review> review;
	
	@OneToMany(mappedBy = "product")
	public List<Variant> variant;
}
