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
	@NotBlank(message = "Mã SKU không được trống")
	public String slug;

	@PastOrPresent(message = "Ngày tạo phải là ngày trong quá khứ hoặc hiện tại")
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	public LocalDateTime createAt = LocalDateTime.now();

	public Integer status;

	// 0: Hết hàng, 1: Còn hàng, 2: Sắp hết hàng
	public Integer stockStatus;

	@Column(name = "content", columnDefinition = "nvarchar(max)")
	@NotBlank(message = "Mô tả sản phẩm không được trống")
	public String content;

	@ManyToOne
	@JoinColumn(name = "category_id")
	@jakarta.validation.constraints.NotNull(message = "Chưa chọn danh mục")
	Category category;

	@ManyToOne
	@JoinColumn(name = "brand_id")
	@jakarta.validation.constraints.NotNull(message = "Chưa chọn thương hiệu")
	Brand brand;

	public String specifications;

	@OneToMany(mappedBy = "product")
	public List<Sale> sale;

	@OneToMany(mappedBy = "product")
	public List<Review> review;

	@OneToMany(mappedBy = "product")
	public List<Variant> variant;

	@OneToMany(mappedBy = "product")
	public List<ImageProduct> imageProduct;
}
