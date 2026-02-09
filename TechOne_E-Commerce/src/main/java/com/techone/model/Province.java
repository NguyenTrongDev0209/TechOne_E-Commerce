package com.techone.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "provinces")
public class Province {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	
	@Column(columnDefinition = "varchar(255)")
	@NotBlank(message = "Code province không được để trống")
	public String code;
	
	@Column(columnDefinition = "nvarchar(255)")
	@NotBlank(message = "Tên tỉnh thành không được để trống")
	public String name;
	
	@OneToMany(mappedBy = "province")
	public List<District> district;
}
