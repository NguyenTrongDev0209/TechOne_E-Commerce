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
@Table(name = "variant_images")
public class VariantImage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	
	@Column(columnDefinition = "varchar(255)")
	public String pathImage;
	
	@FutureOrPresent(message = "Ngày tạo không thể ở quá khứ")
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	public LocalDateTime createAt = LocalDateTime.now();
	
	@ManyToOne
	@JoinColumn(name = "variant_id")
	Variant variant;
}
