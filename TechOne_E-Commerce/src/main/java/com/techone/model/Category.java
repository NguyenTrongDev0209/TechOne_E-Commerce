package com.techone.model;

import java.time.LocalDate;
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
@Table(name = "categories")
public class Category {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	
	@Column(columnDefinition = "nvarchar(255)", unique = true)
	@NotBlank(message = "Tên danh mục không được để trống")
	public String name;
	
	@Column(columnDefinition = "varchar(255)", unique = true)
	@NotBlank(message = "Slug danh mục không được để trống")
	public String slug;
	
	public Boolean status = true;
	//True = active; False = nonActive
	
	@PastOrPresent(message = "Ngày tạo không thể ở tương lai")
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	public LocalDate createAt = LocalDate.now();
	//Lấy ngày tạo tài khoản là ngày hôm đó luôn
	
	public Boolean type = true;
	//true = danh mục sản phẩm, false = danh mục bài viết
	
	@ManyToOne
	@JoinColumn(name = "parent_id")
	public Category parent;
	
	@OneToMany(mappedBy = "parent")
	public List<Category> children;
	
	@OneToMany(mappedBy = "category")
	public List<Post> post;
	
	@OneToMany(mappedBy = "category")
	public List<Product> product;
}
