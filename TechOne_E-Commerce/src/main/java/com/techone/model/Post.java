package com.techone.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "posts")
public class Post {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	
	@Column(columnDefinition = "nvarchar(255)")
	@NotBlank(message = "Tiêu đề không được để trống")
	public String title;
	
	@Column(columnDefinition = "nvarchar(max)")
	@NotBlank(message = "Nội dung không được để trống")
	public String content;
	
	public Boolean status = true;
	//True = active; False = nonActive
	
	@PastOrPresent(message = "Ngày tạo không thể ở tương lai")
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	public LocalDate createAt = LocalDate.now();
	//Lấy ngày tạo tài khoản là ngày hôm đó luôn
	
	public Integer viewCount;
}
